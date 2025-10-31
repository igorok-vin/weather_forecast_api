package com.skyapi.weathernetworkapi.location;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String locationCode) {
        super("No location found with the given location code: " + locationCode);
    }

    public LocationNotFoundException(String countryCode, String cityName) {
        super("No location found with the given country code " + countryCode +" and city name: " + cityName);
    }
}
