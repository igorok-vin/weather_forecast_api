package com.skyapi.weathernetworkapi.realtimeweather;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.location.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RealtimeWeatherService {
    private RealtimeWeatherRepository realtimeWeatherRepository;
    private LocationRepository locationRepository;

    @Autowired
    public RealtimeWeatherService(RealtimeWeatherRepository realtimeWeatherRepository, LocationRepository locationRepository) {
        this.realtimeWeatherRepository = realtimeWeatherRepository;
        this.locationRepository = locationRepository;
    }

    @Cacheable(value = "realtimeWEatherByIpAddressCache", key = "{#location.countryCode, #location.cityName}")
    public RealtimeWeather getByLocation(Location location) throws LocationNotFoundException {
        String countryCode = location.getCountryCode();
        String cityName = location.getCityName();

        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode, cityName);
        if(realtimeWeather == null){
            throw new RealtimeWeatherNotFoundException(countryCode, cityName);
        }
        return realtimeWeather;
    }

    @Cacheable(value ="realtimeWeatherByLocationCodeCache")
    public RealtimeWeather getRealtimeWeatherByLocationCode(String locationCode) throws LocationNotFoundException {
        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByLocationCode(locationCode);
        if(realtimeWeather == null){
            throw new RealtimeWeatherNotFoundException(locationCode);
        }
        return realtimeWeather;
    }

    @CachePut(cacheNames = "realtimeWeatherByLocationCodeCache",key="#locationCode")
    @CacheEvict(cacheNames = "realtimeWEatherByIpAddressCache", allEntries = true)
    public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather) throws LocationNotFoundException {
        Location location = locationRepository.findByCode(locationCode);
        if(location == null){
            throw new LocationNotFoundException(locationCode);
        }
        realtimeWeather.setLocation(location);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        if(location.getRealtimeWeather() == null) {
            location.setRealtimeWeather(realtimeWeather);
            Location updatedLocation = locationRepository.save(location);
            return updatedLocation.getRealtimeWeather();
        }
        return realtimeWeatherRepository.save(realtimeWeather);
    }
}
