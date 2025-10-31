package com.skyapi.weathernetworkapi.realtimeweather;

public class RealtimeWeatherNotFoundException extends RuntimeException {
    public RealtimeWeatherNotFoundException(String locationCode) {
        super("No realtime weather data found with the given location code: " + locationCode);
    }

    public RealtimeWeatherNotFoundException(String countryCode, String cityName) {
        super("No realtime weather data found with the given country code: " + countryCode +" and city name: " + cityName);
    }
}
