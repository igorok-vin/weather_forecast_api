package com.skyapi.weathernetworkapi.hourlyweather;

import com.skyapi.weathernetworkapi.CommonUtility;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherApiController;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherApiController;
import com.skyapi.weathernetworkapi.globalerror.BadRequestException;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherApiController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/v1/hourly")
@Validated
public class HourlyWeatherApiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HourlyWeatherApiController.class);

    private HourlyWeatherService hourlyWeatherService;
    private GeolocationService geolocationService;
    private ModelMapper modelMapper;

    @Autowired
    public HourlyWeatherApiController(HourlyWeatherService hourlyWeatherService, GeolocationService geolocationService, ModelMapper modelMapper) {
        this.hourlyWeatherService = hourlyWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getHourlyWeatherByIpAddress(HttpServletRequest request) throws GeolocatioException {
        String ipAddress = CommonUtility.getIPAddress(request);

            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));
            Location locationFromIP = geolocationService.getLocation(ipAddress);
            List<HourlyWeather> hourlyWeatherList = hourlyWeatherService.getAllHourlyWeatherByLocationBasedOnIPAddress(locationFromIP, currentHour);
            if (hourlyWeatherList.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

        HourlyWeatherListDTO listEntityToListDTO = listEntityToHourlyWeatherListDTO(hourlyWeatherList);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic()).body(addLinksByIpAddress(listEntityToListDTO));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getHourlyWeatherByLocationCode(@PathVariable("locationCode") String locationCode, HttpServletRequest request) throws GeolocatioException {

            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));
            List<HourlyWeather> allHourlyWeatherByLocationCode = hourlyWeatherService.getAllHourlyWeatherByLocationCodeAndCurrentHour(locationCode, currentHour);
            if (allHourlyWeatherByLocationCode.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
        HourlyWeatherListDTO listEntityToListDTO = listEntityToHourlyWeatherListDTO(allHourlyWeatherByLocationCode);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(60,TimeUnit.MINUTES).cachePublic()).body(addLinksByLocationCode(listEntityToListDTO,locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateHourlyWeather(@PathVariable("locationCode") String locationCode, @RequestBody @Valid List<HourlyWeatherDTO> listDTO) throws BadRequestException, GeolocatioException {
        if (listDTO.isEmpty()) {
            throw new BadRequestException("Hourly forecast data cannot be empty");
        }
        List<HourlyWeather> hourlyWeatherList = hourlyWeatherDTO_ToHourlyWeather(listDTO);

            List<HourlyWeather> updateHourlyWeatherByLocationCode = hourlyWeatherService.updateHourlyWeatherByLocationCode(locationCode, hourlyWeatherList);
        HourlyWeatherListDTO listEntityToListDTO = listEntityToHourlyWeatherListDTO(updateHourlyWeatherByLocationCode);
        return ResponseEntity.ok(addLinksByLocationCode(listEntityToListDTO,locationCode));
    }

    private HourlyWeatherListDTO listEntityToHourlyWeatherListDTO(List<HourlyWeather> hourlyWeatherList) {
        Location location = hourlyWeatherList.get(0).getId().getLocation();

        HourlyWeatherListDTO resultListDTO = new HourlyWeatherListDTO();
        resultListDTO.setLocation(location.toString());

        hourlyWeatherList.forEach(hourlyWeather -> {
            HourlyWeatherDTO hourlyWeatherDTO = modelMapper.map(hourlyWeather, HourlyWeatherDTO.class);
            resultListDTO.addHourlyWeatherDTO(hourlyWeatherDTO);
        });
        return resultListDTO;
    }

    private List<HourlyWeather> hourlyWeatherDTO_ToHourlyWeather(List<HourlyWeatherDTO> listDTO) {
        List<HourlyWeather> listHourlyWeather = new ArrayList<>();
        listDTO.forEach(hourlyWeatherInListDTO -> {
            listHourlyWeather.add(modelMapper.map(hourlyWeatherInListDTO, HourlyWeather.class));
        });
        return listHourlyWeather;
    }

    private HourlyWeatherListDTO addLinksByIpAddress (HourlyWeatherListDTO hourlyListDTO) throws GeolocatioException {

        hourlyListDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByIpAddress(null)).withSelfRel());

        hourlyListDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByIPAddress(null)).withRel("realtime"));

        hourlyListDTO.add(linkTo(methodOn(DailyWeatherApiController.class).getListDailyWeatherByIPAddress(null)).withRel("daily_forecast"));

        hourlyListDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIpAddress(null)).withRel("full_forecast"));

        return hourlyListDTO;
    }

    private HourlyWeatherListDTO addLinksByLocationCode (HourlyWeatherListDTO hourlyListDTO, String locationCode) throws GeolocatioException {

        hourlyListDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(locationCode,null)).withSelfRel());

        hourlyListDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(locationCode)).withRel("realtime"));

        hourlyListDTO.add(linkTo(methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(locationCode)).withRel("daily_forecast"));

        hourlyListDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode)).withRel("full_forecast"));

        return hourlyListDTO;
    }
}
