package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.AbstractLocation;
import com.skyapi.weathernetworkapi.common.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class LocationService extends AbstractLocation {

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @CachePut(cacheNames = "locationByCodeCache", key = "#location.code")

    @CacheEvict(cacheNames = "locationListCache", allEntries = true)
    public Location addLocation(Location location) {
        return locationRepository.save(location);
    }

    @Deprecated
    public List<Location> getAllLocations() {
        return locationRepository.findAllUntrashed();
    }

    @Deprecated
    public Page<Location> listByPage(int pageNumber, int pageSize, String sortField) {
        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return locationRepository.findAllUntrashed(pageable);
    }

    @Cacheable("locationListCache")
    public Page<Location> listByPage(int pageNumber, int pageSize, String sortOption, Map<String, Object> filterFields) {
        Sort sort = null;
        String[] splitFields = sortOption.split(",");
        if (splitFields.length > 1) {
            String firstFieldName = splitFields[0];
            String actualFieldName = firstFieldName.replace("-","");
            sort = firstFieldName.startsWith("-") ? Sort.by(actualFieldName).descending() : Sort.by(actualFieldName).ascending();

            for (int i = 1; i < splitFields.length; i++) {
                String nextFieldName = splitFields[i];
                String actualNextFieldName = nextFieldName.replace("-","");
                //and - for multiple sorting
                sort = sort.and(nextFieldName.startsWith("-") ? Sort.by(actualNextFieldName).descending() : Sort.by(actualNextFieldName).ascending());
            }
        } else {
            String actualFieldName = sortOption.replace("-", "");
            sort = sortOption.startsWith("-") ? Sort.by(actualFieldName).descending() : Sort.by(actualFieldName).ascending();
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        return locationRepository.listWithFilter(pageable, filterFields);
    }

    @CachePut(cacheNames = "locationByCodeCache", key = "#locationInRequest.code")
    @CacheEvict(cacheNames = "locationListCache", allEntries = true)
    public Location updateLocation(Location locationInRequest) {
        String code = locationInRequest.getCode();
        Location locationInDB = locationRepository.findByCode(code);
        if (locationInDB == null) {
            throw new LocationNotFoundException(code);
        } else {
            locationInDB.setCityName(locationInRequest.getCityName());
            locationInDB.setCountryCode(locationInRequest.getCountryCode());
            locationInDB.setCountryName(locationInRequest.getCountryName());
            locationInDB.setRegionName(locationInRequest.getRegionName());
            locationInDB.setEnabled(locationInRequest.isEnabled());
            return locationRepository.save(locationInDB);
        }
    }

    @CacheEvict(cacheNames = {"locationListCache","locationByCodeCache"}, allEntries = true)
    public void trashedLocation(String code) {
        Location locationInDB = locationRepository.findByCode(code);
        if (locationInDB == null) {
            throw new LocationNotFoundException(code);
        }
        locationRepository.trashedByCode(code);
    }

}
