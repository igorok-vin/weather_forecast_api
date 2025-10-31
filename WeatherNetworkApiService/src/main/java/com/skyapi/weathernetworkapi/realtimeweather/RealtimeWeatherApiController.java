package com.skyapi.weathernetworkapi.realtimeweather;

import com.skyapi.weathernetworkapi.CommonUtility;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherApiController;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherApiController;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherApiController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/realtime")
public class RealtimeWeatherApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeWeatherApiController.class);

    private GeolocationService geolocationService;
    private RealtimeWeatherService realtimeWeatherService;
    private ModelMapper modelMapper;

    @Autowired
    public RealtimeWeatherApiController(GeolocationService geolocationService, RealtimeWeatherService realtimeWeatherService, ModelMapper modelMapper) {
        this.geolocationService = geolocationService;
        this.realtimeWeatherService = realtimeWeatherService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getRealTimeWeatherByIPAddress(HttpServletRequest request) throws GeolocatioException {
        String ipAddress = CommonUtility.getIPAddress(request);
        Location locationFromIP = geolocationService.getLocation(ipAddress);
        RealtimeWeather realtimeWeather = realtimeWeatherService.getByLocation(locationFromIP);

        return ResponseEntity.ok().header("Date", CommonUtility.responseHeaderDateAndTime()).body(addLinksByIpAddress(entityToRealtimeWeatherDTO(realtimeWeather)));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getRealTimeWeatherByLocationCode(@PathVariable String locationCode) throws GeolocatioException {
        RealtimeWeather realtimeWeather = realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode);
        RealtimeWeatherDTO entityToDTO = entityToRealtimeWeatherDTO(realtimeWeather);

        String lastUpdateTime = realtimeWeather.getLastUpdated();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(lastUpdateTime, formatter);
        Instant lastModifiedTime = localDateTime.atZone(ZoneId.of("UTC")).toInstant();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        /*Може бути null тому конвертуєм в Optional*/
        Optional<String> ifModifiedSince =Optional.ofNullable(servletRequest.getHeader("If-Modified-Since"));
        if(ifModifiedSince.isPresent()) {
            Instant ifModifiedSinceTime = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).parse(ifModifiedSince.get()));
            if(!lastModifiedTime.isAfter(ifModifiedSinceTime)) {
                return ResponseEntity.status(304).build();
            }
        }

        return ResponseEntity.ok().header("Date", CommonUtility.responseHeaderDateAndTime()).cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES).cachePublic()).lastModified(lastModifiedTime).body(addLinksByLocationCode(entityToDTO,locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateRealTimeWeatherByLocationCode(@PathVariable("locationCode") String locationCode, @RequestBody @Valid RealtimeWeatherDTO realtimeWeatherInRequestDTO) throws GeolocatioException {
        RealtimeWeather realtimeWeather = dtoToRealtimeWeather(realtimeWeatherInRequestDTO);
        realtimeWeather.setLocationCode(locationCode);

        RealtimeWeather realtimeWeatherUpdated = realtimeWeatherService.update(locationCode, realtimeWeather);

        RealtimeWeatherDTO entityToDTO = entityToRealtimeWeatherDTO(realtimeWeatherUpdated);

        return ResponseEntity.ok(addLinksByLocationCode(entityToDTO,locationCode));
    }

    private RealtimeWeatherDTO entityToRealtimeWeatherDTO(RealtimeWeather realtimeWeather) {
        return modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);
    }

    private RealtimeWeather dtoToRealtimeWeather(RealtimeWeatherDTO realtimeWeatherDTO) {
        return modelMapper.map(realtimeWeatherDTO, RealtimeWeather.class);
    }

    private RealtimeWeatherDTO addLinksByIpAddress(RealtimeWeatherDTO realtimeWeatherDTO) throws GeolocatioException {

        realtimeWeatherDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByIPAddress(null)).withSelfRel());

        realtimeWeatherDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByIpAddress(null)).withRel("hourly_forecast"));

        realtimeWeatherDTO.add(linkTo(methodOn(DailyWeatherApiController.class).getListDailyWeatherByIPAddress(null)).withRel("daily_forecast"));

        realtimeWeatherDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIpAddress(null)).withRel("full_forecast"));

        return realtimeWeatherDTO;
    }

    private RealtimeWeatherDTO addLinksByLocationCode(RealtimeWeatherDTO realtimeWeatherDTO, String locationCode) throws GeolocatioException {

        realtimeWeatherDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(locationCode)).withSelfRel());

        realtimeWeatherDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(locationCode,null)).withRel("hourly_forecast"));

        realtimeWeatherDTO.add(linkTo(methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(locationCode)).withRel("daily_forecast"));

        realtimeWeatherDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode)).withRel("full_forecast"));

        return realtimeWeatherDTO;
    }
}
