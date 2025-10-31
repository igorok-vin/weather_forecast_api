package com.skyapi.weathernetworkapi.base;

import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherApiController;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherApiController;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherApiController;
import com.skyapi.weathernetworkapi.location.LocationApiController;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherApiController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class MainController {

    @GetMapping("/")
    public ResponseEntity<RootEntity> handleBaseURI() throws GeolocatioException {
        return ResponseEntity.ok(createRootEntity());
    }

    private RootEntity createRootEntity() throws GeolocatioException {
        RootEntity rootEntity = new RootEntity();

        String locationURL = linkTo(methodOn(LocationApiController.class).getAllLocations()).toString();
        rootEntity.setLocationsURL(locationURL);

        String locationByCodeURL = linkTo(methodOn(LocationApiController.class).getLocationByCode(null)).toString();
        rootEntity.setLocationByCodeUrl(locationByCodeURL);

        String realtimeWeatherByIpURL = linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByIPAddress(null)).toString();
        rootEntity.setRealtimeWeatherByIpURL(realtimeWeatherByIpURL);

        String realtimeWeatherByCodeURL = linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(null)).toString();
        rootEntity.setRealtimeWeatherByCodeURL(realtimeWeatherByCodeURL);

        String hourlyForecastByIpURL = linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByIpAddress(null)).toString();
        rootEntity.setHourlyForecastByIpURL(hourlyForecastByIpURL);

        String hourlyForecastByCodeURL = linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(null,null)).toString();
        rootEntity.setHourlyForecastByCodeURL(hourlyForecastByCodeURL);

        String dailyForecastByIpURL = linkTo(methodOn(DailyWeatherApiController.class).getListDailyWeatherByIPAddress(null)).toString();
        rootEntity.setDailyForecastByIpURL(dailyForecastByIpURL);

        String dailyForecastByCodeURL = linkTo(methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(null)).toString();
        rootEntity.setDailyForecastByCodeURL(dailyForecastByCodeURL);

        String fullWeatherByIpURL = linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIpAddress(null)).toString();
        rootEntity.setFullWeatherByIpURL(fullWeatherByIpURL);

        String fullWeatherByCodeURL = linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(null)).toString();
        rootEntity.setFullWeatherByCodeURL(fullWeatherByCodeURL);

        return rootEntity;
    }

}
