package com.skyapi.weathernetworkapi.dailyweather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@JsonPropertyOrder({"day_of_month", "month", "min_temperature", "max_temperature", "precipitation", "status"})
public class DailyWeatherDTO {

    @Range(min = 1, max = 31, message = "Day of month must be in between 1-31")
    private int dayOfMonth;

    @Range(min = 1, max = 12,message = "Month must be in the range of 1 to 12")
    private int month;

    @Range(min = -50, max = 50,message = "Temperature must be in the range of -50 to 50")
    private int minTemperature;

    @Range(min = -50, max = 50,message = "Temperature must be in the range of -50 to 50")
    private int maxTemperature;

    @Range(min = 0, max = 100,message = "Precipitation must be in the range of 0 to 100")
    private int precipitation;

    @NotBlank(message = "Status must not be empty")
    @Length(min = 3, max = 50, message="Status must be between 3-50 characters")
    private String status;

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(int precipitation) {
        this.precipitation = precipitation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DailyWeatherDTO dayOfMonth(int dayOfMonth) {
        setDayOfMonth(dayOfMonth);
        return this;
    }

    public DailyWeatherDTO month (int month) {
        setMonth(month);
        return this;
    }

    public DailyWeatherDTO min_temperature(int min_temperature) {
        setMinTemperature(min_temperature);
        return this;
    }

    public DailyWeatherDTO max_temperature(int max_temperature) {
        setMaxTemperature(max_temperature);
        return this;
    }

    public DailyWeatherDTO precipitation(int precipitation) {
        setPrecipitation(precipitation);
        return this;
    }

    public DailyWeatherDTO status (String status) {
        setStatus(status);
        return this;
    }

    @Override
    public String toString() {
        return "DailyWeatherDTO{" +
                "dayOfMonth=" + dayOfMonth +
                ", month=" + month +
                ", minTemperature=" + minTemperature +
                ", maxTemperature=" + maxTemperature +
                ", precipitation=" + precipitation +
                ", status='" + status + '\'' +
                '}';
    }
}
