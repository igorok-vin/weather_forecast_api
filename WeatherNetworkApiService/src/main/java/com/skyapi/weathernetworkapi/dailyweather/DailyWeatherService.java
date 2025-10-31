package com.skyapi.weathernetworkapi.dailyweather;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.location.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyWeatherService {

    private DailyWeatherRepository dailyWeatherRepository;
    private LocationRepository locationRepository;

    @Autowired

    public DailyWeatherService(DailyWeatherRepository dailyWeatherRepository, LocationRepository locationRepository) {
        this.dailyWeatherRepository = dailyWeatherRepository;
        this.locationRepository = locationRepository;
    }

    @Cacheable(value = "dailyWeatherByIpAddressCache", key = "{#location.countryCode, #location.cityName}")
    public List<DailyWeather> getDailyWeatherByLocation(Location location) {
        String countryCode = location.getCountryCode();
        String cityName = location.getCityName();

        Location locationInDB = locationRepository.findByCountryCodeAndCityName(countryCode, cityName);
        if (locationInDB == null) {
            throw new LocationNotFoundException(countryCode);
        }
        return dailyWeatherRepository.findByLocationCode(locationInDB.getCode());
    }

    @Cacheable(value = "dailyWeatherByCodeCache",key="#locationCode")
    public List<DailyWeather> getDailyWeatherByLocationCode(String locationCode) throws LocationNotFoundException {
        Location locationInDB = locationRepository.findByCode(locationCode);
        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }
        return dailyWeatherRepository.findByLocationCode(locationCode);
    }

    @CachePut(cacheNames = "dailyWeatherByCodeCache",key="#locationCode")
    @CacheEvict(cacheNames = "dailyWeatherByIpAddressCache", allEntries = true)
    public List<DailyWeather> updateDailyWeatherByLocationCode(String locationCode, List<DailyWeather> dailyWeatherInRequest) throws LocationNotFoundException {
        Location locationInDB = locationRepository.findByCode(locationCode);
        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }

        for (DailyWeather dailyWeather : dailyWeatherInRequest) {
            dailyWeather.getId().setLocation(locationInDB);
        }
        List<DailyWeather> dailyWeatherListInDB = locationInDB.getListDailyWeather();
        dailyWeatherListInDB.retainAll(dailyWeatherInRequest);

        return (List<DailyWeather>) dailyWeatherRepository.saveAll(dailyWeatherInRequest);
    }
    
}
