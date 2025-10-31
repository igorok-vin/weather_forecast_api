package com.skyapi.weathernetworkapi.hourlyweather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"location", "hourly_forecast"})
public class HourlyWeatherListDTO extends RepresentationModel<HourlyWeatherListDTO> {

    private String location;

    @JsonProperty("hourly_forecast")
    private List<HourlyWeatherDTO> hourlyWeatherDTOList = new ArrayList<>();

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<HourlyWeatherDTO> getHourlyWeatherDTOList() {
        return hourlyWeatherDTOList;
    }

    public void setHourlyWeatherDTOList(List<HourlyWeatherDTO> hourlyWeatherDTOList) {
        this.hourlyWeatherDTOList = hourlyWeatherDTOList;
    }

    public void addHourlyWeatherDTO(HourlyWeatherDTO hourlyWeatherDTO) {
        this.hourlyWeatherDTOList.add(hourlyWeatherDTO);
    }
}
