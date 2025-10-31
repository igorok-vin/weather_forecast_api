package com.skyapi.weathernetworkapi.dailyweather;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DailyWeatherListDTO {

    private String location;

    @JsonProperty("daily_forecast")
    private List<DailyWeatherDTO> dailyWeatherDTOList = new ArrayList<>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<DailyWeatherDTO> getDailyWeatherDTOList() {
        return dailyWeatherDTOList;
    }

    public void setDailyWeatherDTOList(List<DailyWeatherDTO> dailyWeatherDTOList) {
        this.dailyWeatherDTOList = dailyWeatherDTOList;
    }

    public void addDailyWeather (DailyWeatherDTO dailyWeatherDTO) {
        this.dailyWeatherDTOList.add(dailyWeatherDTO);
    }
}
