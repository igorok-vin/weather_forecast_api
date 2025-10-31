package com.skyapi.weathernetworkapi.dailyweather;

import com.skyapi.weathernetworkapi.CommonUtility;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherApiController;
import com.skyapi.weathernetworkapi.globalerror.BadRequestException;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherApiController;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherApiController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/daily")
@Validated
public class DailyWeatherApiController {

    private DailyWeatherService dailyWeatherService;
    private GeolocationService geolocationService;
    private ModelMapper modelMapper;

    @Autowired
    public DailyWeatherApiController(DailyWeatherService dailyWeatherService, GeolocationService geolocationService, ModelMapper modelMapper) {
        this.dailyWeatherService = dailyWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getListDailyWeatherByIPAddress(HttpServletRequest request) throws GeolocatioException {
        String ipAddress = CommonUtility.getIPAddress(request);
        Location location = geolocationService.getLocation(ipAddress);
        System.out.println("location: " + location);

        List<DailyWeather> dailyWeathers = dailyWeatherService.getDailyWeatherByLocation(location);

        if (dailyWeathers.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        DailyWeatherListDTO listEntityToListDTO = listEntityToDailyWeatherListDTO(dailyWeathers);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(360, TimeUnit.MINUTES).cachePublic()).body(addLinksByIpAddress(listEntityToListDTO));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getDailyWeatherByLocationCode(@PathVariable String locationCode) throws GeolocatioException {
        List<DailyWeather> allDailyWeathersByLocationCode = dailyWeatherService.getDailyWeatherByLocationCode(locationCode);
        if (allDailyWeathersByLocationCode.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        DailyWeatherListDTO listEntityToListDTO = listEntityToDailyWeatherListDTO(allDailyWeathersByLocationCode);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(360, TimeUnit.MINUTES).cachePublic()).body(addLinksByLocationCode(listEntityToListDTO,locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateDailyWeatherByLocationCode(@PathVariable("locationCode") String locationCode, @RequestBody @Valid List<DailyWeatherDTO> listDTO) throws BadRequestException, GeolocatioException {

        if(listDTO.isEmpty()) {
            throw new BadRequestException("Daily forecast data cannot be empty");
        }
        List<DailyWeather> dailyWeathers = dailyWeatherDTOTODailyWeather(listDTO);
        List<DailyWeather> updatedDailyWeathers = dailyWeatherService.updateDailyWeatherByLocationCode(locationCode, dailyWeathers);
        DailyWeatherListDTO listEntityToListDTO = listEntityToDailyWeatherListDTO(updatedDailyWeathers);

        return ResponseEntity.ok().body(addLinksByLocationCode(listEntityToListDTO,locationCode));
    }

    private List<DailyWeather> dailyWeatherDTOTODailyWeather(List<DailyWeatherDTO> listDTO) {
        List<DailyWeather> listDailyWeather = new ArrayList<>();
        listDTO.forEach(dailyWeatherInListDTO -> {
            listDailyWeather.add(modelMapper.map(dailyWeatherInListDTO, DailyWeather.class));
        });
        return listDailyWeather;
    }

    private DailyWeatherListDTO listEntityToDailyWeatherListDTO(List<DailyWeather> dailyWeatherList) {
        Location location = dailyWeatherList.get(0).getId().getLocation();

        DailyWeatherListDTO dailyWeatherListDTO = new DailyWeatherListDTO();
        dailyWeatherListDTO.setLocation(location.toString());

        dailyWeatherList.forEach(dailyWeather -> {
            DailyWeatherDTO dailyWeatherDTO = modelMapper.map(dailyWeather, DailyWeatherDTO.class);
            dailyWeatherListDTO.addDailyWeather(dailyWeatherDTO);
        });
        return dailyWeatherListDTO;
    }

    private EntityModel<DailyWeatherListDTO> addLinksByIpAddress(DailyWeatherListDTO dailyWeatherListDTO) throws GeolocatioException {
        EntityModel<DailyWeatherListDTO> entityModel = EntityModel.of(dailyWeatherListDTO);
        entityModel.add(linkTo(methodOn(DailyWeatherApiController.class).getListDailyWeatherByIPAddress(null)).withSelfRel());

        entityModel.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByIPAddress(null)).withRel("realtime"));

        entityModel.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByIpAddress(null)).withRel("hourly_forecast"));

        entityModel.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIpAddress(null)).withRel("full_forecast"));
        return entityModel;
    }

    private EntityModel<DailyWeatherListDTO> addLinksByLocationCode (DailyWeatherListDTO dailyWeatherListDTO, String locationCode) throws GeolocatioException {
        return EntityModel.of(dailyWeatherListDTO)
                .add(linkTo(methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(locationCode)).withSelfRel())
                .add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(locationCode)).withRel("realtime"))
                .add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(locationCode,null)).withRel("hourly_forecast"))
                .add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode)).withRel("full_forecast"));
    }
}
