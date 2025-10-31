package com.skyapi.weathernetworkapi;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.location.LocationRepository;
import org.springframework.cache.annotation.Cacheable;

public abstract class AbstractLocation {

    protected LocationRepository locationRepository;

    @Cacheable(cacheNames = "locationByCodeCache", key = "#code")
    public Location getLocationByCode(String code) {
        Location location = locationRepository.findByCode(code);
        if (location == null) {
            throw new LocationNotFoundException(code);
        }
        return location;
    }
}
