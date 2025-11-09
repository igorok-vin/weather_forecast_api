package com.skyapi.weathernetworkapi.common.dailyweather;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "weather_daily")
public class DailyWeather implements Serializable {

    @EmbeddedId
    private DailyWeatherId id = new DailyWeatherId();

    private int minTemperature;
    private int maxTemperature;
    private int precipitation;

    @Column(length = 50)
    private String status;

    public DailyWeatherId getId() {
        return id;
    }

    public void setId(DailyWeatherId id) {
        this.id = id;
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

    public DailyWeather id(Location location, int dayOfMonth, int month) {
        this.id.setLocation(location);
        this.id.setDayOfMonth(dayOfMonth);
        this.id.setMonth(month);
        return this;
    }

    public DailyWeather dayOfMonth(int dayOfMonth) {
        this.id.setDayOfMonth(dayOfMonth);
        return this;
    }

    public DailyWeather month(int month) {
        this.id.setMonth(month);
        return this;
    }

    public DailyWeather minTemperature(int minTemperature) {
        setMinTemperature(minTemperature);
        return this;
    }

    public DailyWeather maxTemperature(int maxTemperature) {
        setMaxTemperature(maxTemperature);
        return this;
    }

    public DailyWeather precipitation(int precipitation) {
        setPrecipitation(precipitation);
        return this;
    }

    public DailyWeather status(String status) {
        setStatus(status);
        return this;
    }

    public DailyWeather location(Location location) {
        this.id.setLocation(location);
        return this;
    }

    @Override
    public String toString() {
        return "DailyWeather{" +
                "id=" + id +
                ", minTemperature=" + minTemperature +
                ", maxTemperature=" + maxTemperature +
                ", precipitation=" + precipitation +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyWeather that = (DailyWeather) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
