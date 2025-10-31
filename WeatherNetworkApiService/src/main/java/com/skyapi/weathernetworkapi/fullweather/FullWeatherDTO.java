package com.skyapi.weathernetworkapi.fullweather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherDTO;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherDTO;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public class FullWeatherDTO {

    private String location;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = RealtimeWeatherFieldFilter.class)

    @Valid
    private RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();

    @JsonProperty("hourly_forecast")
    @Valid
    private List<HourlyWeatherDTO> listHourlyWeather = new ArrayList<>();

    @JsonProperty("daily_forecast")
    @Valid
    private List<DailyWeatherDTO> listDailyWeather = new ArrayList<>();

    public FullWeatherDTO() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public RealtimeWeatherDTO getRealtimeWeather() {
        return realtimeWeather;
    }

    public void setRealtimeWeather(RealtimeWeatherDTO realtimeWeather) {
        this.realtimeWeather = realtimeWeather;
    }

    public List<HourlyWeatherDTO> getListHourlyWeather() {
        return listHourlyWeather;
    }

    public void setListHourlyWeather(List<HourlyWeatherDTO> listHourlyWeather) {
        this.listHourlyWeather = listHourlyWeather;
    }

    public List<DailyWeatherDTO> getListDailyWeather() {
        return listDailyWeather;
    }

    public void setListDailyWeather(List<DailyWeatherDTO> listDailyWeather) {
        this.listDailyWeather = listDailyWeather;
    }
}
