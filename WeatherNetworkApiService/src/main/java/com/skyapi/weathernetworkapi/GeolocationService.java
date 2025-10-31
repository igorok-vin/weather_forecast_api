package com.skyapi.weathernetworkapi;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GeolocationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationService.class);
    private IP2Location ip2Location = new IP2Location();
    private String DBPath = "/ip2locationdatabase/IP2LOCATIONLITEDB3.BIN";

    public GeolocationService() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(DBPath);
            byte[] data = inputStream.readAllBytes();
            ip2Location.Open(data);
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    @Cacheable(cacheNames = "geolocationCache")
    public Location getLocation(String ipAddress) throws GeolocatioException {
        try {
            IPResult result = ip2Location.IPQuery(ipAddress);
            if(!"OK".equals(result.getStatus())) {
                throw new GeolocatioException("Geolocation failed with status: " + result.getStatus());
            }
            return new Location(result.getCity(), result.getRegion(), result.getCountryLong(), result.getCountryShort());
        } catch (IOException e) {
            throw new GeolocatioException("Error query IP database",e);
        }
    }
}
