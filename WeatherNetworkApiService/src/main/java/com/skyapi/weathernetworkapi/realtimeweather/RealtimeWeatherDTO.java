package com.skyapi.weathernetworkapi.realtimeweather;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.hateoas.RepresentationModel;

@JsonPropertyOrder({"location","temperature","humidity","precipitation","status","last_updated","wind_speed"})
public class RealtimeWeatherDTO extends RepresentationModel<RealtimeWeatherDTO> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String location;

    @Range(min = -50, max = 50,message = "Temperature must be in the range of -50 to 50")
    private int temperature;

    @Range(min = 0, max = 100,message = "Humidity must be in the range of 0 to 100")
    private int humidity;

    @Range(min = 0, max = 100,message = "Precipitation must be in the range of 0 to 100")
    private int precipitation;

    @Range(min = 0, max = 200,message = "Wind speed must be in the range of 0 to 200 km/h")
    @JsonProperty("wind_speed")//в якому вигляді буде вивід на екран цього поля
    private int windSpeed;

    @NotBlank(message = "Status must not be empty")
    @Length(min = 3, max = 50, message="Status must be between 3-50 characters")
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private String lastUpdated;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(int precipitation) {
        this.precipitation = precipitation;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated;
    }
}