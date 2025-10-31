package com.skyapi.weathernetworkapi.hourlyweather;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.location.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HourlyWeatherService {

    private HourlyWeatherRepository hourlyWeatherRepository;
    private LocationRepository locationRepository;

    @Autowired
    public HourlyWeatherService(HourlyWeatherRepository hourlyWeatherRepository, LocationRepository locationRepository) {
        this.hourlyWeatherRepository = hourlyWeatherRepository;
        this.locationRepository = locationRepository;
    }

    @Cacheable(cacheNames = "hourlyWeatherByIpAddressCache", key="{#location.countryCode, #location.cityName, #currenHour}")
    public List<HourlyWeather> getAllHourlyWeatherByLocationBasedOnIPAddress(Location location, int currenHour) throws LocationNotFoundException {
        String countryCode = location.getCountryCode();
        String cityName = location.getCityName();

        Location locationInDB = locationRepository.findByCountryCodeAndCityName(countryCode, cityName);
        if (locationInDB == null) {
            throw new LocationNotFoundException(countryCode, cityName);
        }
        return hourlyWeatherRepository.findHourlyWeatherByLocationCodeAndCurrentHour(locationInDB.getCode(), currenHour);
    }

    @Cacheable(cacheNames = "hourlyWeatherByCodeCache")
    public List<HourlyWeather> getAllHourlyWeatherByLocationCodeAndCurrentHour(String locationCode, int hourOfDay) throws LocationNotFoundException {
        Location locationInDB = locationRepository.findByCode(locationCode);
        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }
        return hourlyWeatherRepository.findHourlyWeatherByLocationCodeAndCurrentHour(locationInDB.getCode(), hourOfDay);
    }

    public List<HourlyWeather> getAllHourlyWeatherByLocationCode(String locationCode) throws LocationNotFoundException {
        Location locationInDB = locationRepository.findByCode(locationCode);
        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }
        return hourlyWeatherRepository.findHourlyWeatherByLocationCode(locationInDB.getCode());
    }

    @CacheEvict(cacheNames = "{hourlyWeatherByIpCache, hourlyWeatherByCodeCache}", allEntries = true)
    public List<HourlyWeather> updateHourlyWeatherByLocationCode(String locationCode, List<HourlyWeather> hourlyWeatherInRequest) throws LocationNotFoundException {
        Location location = locationRepository.findByCode(locationCode);
        if (location == null) {
            throw new LocationNotFoundException(locationCode);
        }

        for(HourlyWeather item : hourlyWeatherInRequest) {
            item.getId().setLocation(location);
        }

        List<HourlyWeather> hourlyWeatherListInDB = location.getListHourlyWeather();
        hourlyWeatherListInDB.retainAll(hourlyWeatherInRequest);

        return (List<HourlyWeather>) hourlyWeatherRepository.saveAll(hourlyWeatherInRequest);
    }
}
