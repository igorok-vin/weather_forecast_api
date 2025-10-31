package com.skyapi.weathernetworkapi.fullweather;

import com.skyapi.weathernetworkapi.CommonUtility;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.globalerror.BadRequestException;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/full")
public class FullWeatherApiController {

    private GeolocationService geolocationService;
    private FullWeatherService fullWeatherService;
    private ModelMapper modelMapper;
    private FullWeatherModelAssembler fullWeatherModelAssembler;

    @Autowired
    public FullWeatherApiController(GeolocationService geolocationService, FullWeatherService fullWeatherService, ModelMapper modelMapper, FullWeatherModelAssembler fullWeatherModelAssembler) {
        this.geolocationService = geolocationService;
        this.fullWeatherService = fullWeatherService;
        this.modelMapper = modelMapper;
        this.fullWeatherModelAssembler = fullWeatherModelAssembler;
    }

    @GetMapping
    public ResponseEntity<?> getFullWeatherByIpAddress(HttpServletRequest request) throws GeolocatioException {
        String ipAddress = CommonUtility.getIPAddress(request);
        Location getLocationFromIP = geolocationService.getLocation(ipAddress);
        Location getLocationInDB = fullWeatherService.getLocationInDBByLocationFromIP(getLocationFromIP);
        FullWeatherDTO entityToFullWeatherDTO = entityToFullWeatherDTO(getLocationInDB);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic()).body(fullWeatherModelAssembler.toModel(entityToFullWeatherDTO));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable String locationCode) {
        Location fullWeatherByLocationCode = fullWeatherService.getLocationByCode(locationCode);
        FullWeatherDTO entityToDTO = entityToFullWeatherDTO(fullWeatherByLocationCode);

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic()).body(addLinksByLocation(entityToDTO, locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateFullWeatherByLocationCode(@PathVariable String locationCode, @Valid @RequestBody  FullWeatherDTO fullWeatherDTO) throws BadRequestException {
        
        if(fullWeatherDTO.getRealtimeWeather() == null) {
            throw new BadRequestException("Realtime weather data cannot be empty");
        }

        if(fullWeatherDTO.getListHourlyWeather().isEmpty()) {
            throw new BadRequestException("Hourly weather data cannot be empty");
        }

        if(fullWeatherDTO.getListDailyWeather().isEmpty()) {
            throw new BadRequestException("Daily weather data cannot be empty");
        }

        Location locationInRequest = dtoToEntity(fullWeatherDTO);

        Location updatedLocation = fullWeatherService.update(locationCode, locationInRequest);

        FullWeatherDTO entityToDTO = entityToFullWeatherDTO(updatedLocation);

        return ResponseEntity.ok().body(addLinksByLocation(entityToDTO, locationCode));
    }

    private FullWeatherDTO entityToFullWeatherDTO(Location location) {
        FullWeatherDTO dto = modelMapper.map(location, FullWeatherDTO.class);
        dto.getRealtimeWeather().setLocation(null);
        return dto;
    }

    private Location dtoToEntity(FullWeatherDTO dto) {
        return modelMapper.map(dto, Location.class);
    }

private EntityModel<FullWeatherDTO>addLinksByLocation(FullWeatherDTO fullWeatherDTO, String locationCode) {
        return EntityModel.of(fullWeatherDTO).add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode)).withSelfRel());
}
}

