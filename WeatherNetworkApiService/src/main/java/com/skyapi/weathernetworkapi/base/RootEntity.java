package com.skyapi.weathernetworkapi.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"locations_url","location_by_code_url","realtime_weather_by_ip_url","realtime_weather_by_code_url","hourly_forecast_by_ip_url","hourly_forecast_by_code_url", "daily_forecast_by_ip_url","daily_forecast_by_code_url","full_weather_by_ip_url","full_weather_by_code_url"})
public class RootEntity {

    private String locationsURL;
    private String locationByCodeUrl;
    private String realtimeWeatherByIpURL;
    private String realtimeWeatherByCodeURL;
    private String hourlyForecastByIpURL;
    private String hourlyForecastByCodeURL;
    private String dailyForecastByIpURL;
    private String dailyForecastByCodeURL;
    private String fullWeatherByIpURL;
    private String fullWeatherByCodeURL;

    public String getLocationsURL() {
        return locationsURL;
    }

    public void setLocationsURL(String locationsURL) {
        this.locationsURL = locationsURL;
    }

    public String getLocationByCodeUrl() {
        return locationByCodeUrl;
    }

    public void setLocationByCodeUrl(String locationByCodeUrl) {
        this.locationByCodeUrl = locationByCodeUrl;
    }

    public String getRealtimeWeatherByIpURL() {
        return realtimeWeatherByIpURL;
    }

    public void setRealtimeWeatherByIpURL(String realtimeWeatherByIpURL) {
        this.realtimeWeatherByIpURL = realtimeWeatherByIpURL;
    }

    public String getRealtimeWeatherByCodeURL() {
        return realtimeWeatherByCodeURL;
    }

    public void setRealtimeWeatherByCodeURL(String realtimeWeatherByCodeURL) {
        this.realtimeWeatherByCodeURL = realtimeWeatherByCodeURL;
    }

    public String getHourlyForecastByIpURL() {
        return hourlyForecastByIpURL;
    }

    public void setHourlyForecastByIpURL(String hourlyForecastByIpURL) {
        this.hourlyForecastByIpURL = hourlyForecastByIpURL;
    }

    public String getHourlyForecastByCodeURL() {
        return hourlyForecastByCodeURL;
    }

    public void setHourlyForecastByCodeURL(String hourlyForecastByCodeURL) {
        this.hourlyForecastByCodeURL = hourlyForecastByCodeURL;
    }

    public String getDailyForecastByIpURL() {
        return dailyForecastByIpURL;
    }

    public void setDailyForecastByIpURL(String dailyForecastByIpURL) {
        this.dailyForecastByIpURL = dailyForecastByIpURL;
    }

    public String getDailyForecastByCodeURL() {
        return dailyForecastByCodeURL;
    }

    public void setDailyForecastByCodeURL(String dailyForecastByCodeURL) {
        this.dailyForecastByCodeURL = dailyForecastByCodeURL;
    }

    public String getFullWeatherByIpURL() {
        return fullWeatherByIpURL;
    }

    public void setFullWeatherByIpURL(String fullWeatherByIpURL) {
        this.fullWeatherByIpURL = fullWeatherByIpURL;
    }

    public String getFullWeatherByCodeURL() {
        return fullWeatherByCodeURL;
    }

    public void setFullWeatherByCodeURL(String fullWeatherByCodeURL) {
        this.fullWeatherByCodeURL = fullWeatherByCodeURL;
    }
}
