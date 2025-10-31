package com.skyapi.weathernetworkapi.fullweather;

import com.skyapi.weathernetworkapi.AbstractLocation;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.location.LocationRepository;
import com.skyapi.weathernetworkapi.location.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FullWeatherService extends AbstractLocation {

    public LocationService locationService;

    @Autowired
    public FullWeatherService(LocationRepository locationRepository, LocationService locationService) {
        this.locationRepository = locationRepository;
        this.locationService = locationService;
    }

    @Cacheable(cacheNames = "fullWeatherByIpAddressCache", key="{#locationFromIP.cityName,#locationFromIP.countryCode}")
    public Location getLocationInDBByLocationFromIP(Location locationFromIP) {
        String cityName = locationFromIP.getCityName();
        String countryCode = locationFromIP.getCountryCode();

        Location locationInDB = locationRepository.findByCountryCodeAndCityName(countryCode,cityName );
        if (locationInDB == null) {
            throw new LocationNotFoundException(countryCode,cityName);
        }
        return locationInDB;
    }

    @Caching(
            put = {@CachePut(cacheNames = "locationByCodeCache", key = "#locationCode")},
            evict = {@CacheEvict(cacheNames = {"fullWeatherByIpAddressCache", "realtimeWeatherByIpAddressCache", "hourlyWeatherByIpAddressCache", "dailyWeatherByIpAddressCache"}, allEntries = true),
                    @CacheEvict(cacheNames = {"realtimeWeatherByLocationCodeCache", "hourlyWeatherByCodeCache","dailyWeatherByCodeCache"}, key = "#locationCode")}
    )
    public Location update(String locationCode, Location locationInRequest) throws LocationNotFoundException {
        Location locationInDB = locationRepository.findByCode(locationCode);

        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }

        setRealtimeWeather(locationInRequest, locationInDB);

        setDailyAndHourlyWeatherData(locationInRequest, locationInDB);

        locationInRequest.setCode(locationInDB.getCode());
        locationInRequest.setCityName(locationInDB.getCityName());
        locationInRequest.setRegionName(locationInDB.getRegionName());
        locationInRequest.setCountryCode(locationInDB.getCountryCode());
        locationInRequest.setCountryName(locationInDB.getCountryName());
        locationInRequest.setEnabled(locationInDB.isEnabled());
        locationInRequest.setTrashed(locationInDB.isTrashed());
     
        return locationRepository.save(locationInRequest);
    }

    private void setDailyAndHourlyWeatherData(Location locationInRequest, Location locationInDB) {
        List<DailyWeather> listDailyWeatherInRequest = locationInRequest.getListDailyWeather();
        listDailyWeatherInRequest.forEach(dailyWeather -> dailyWeather.getId().setLocation(locationInDB));

        List<HourlyWeather> listHourlyWeatherInRequest = locationInRequest.getListHourlyWeather();
        listHourlyWeatherInRequest.forEach(hourlyWeather -> hourlyWeather.getId().setLocation(locationInDB));
    }

    private void setRealtimeWeather(Location locationInRequest, Location locationInDB) {
        RealtimeWeather realtimeWeatherInDB = locationInDB.getRealtimeWeather();

        RealtimeWeather realtimeWeatherInRequest = locationInRequest.getRealtimeWeather();

        if (realtimeWeatherInRequest.getStatus() != null) {
            realtimeWeatherInRequest.setLocation(locationInDB);
            realtimeWeatherInRequest.setLastUpdated(RealtimeWeather.setTime());
        }

        if(realtimeWeatherInRequest.getStatus()==null) {
            locationInRequest.setRealtimeWeather(realtimeWeatherInDB);
        }
    }
}
