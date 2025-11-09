package com.skyapi.weathernetworkapi.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name="realtime_weather")
public class RealtimeWeather implements Serializable {
    @Id
    @Column(name="location_code")
    @JsonIgnore
    private String locationCode;

    private int temperature;
    private int humidity;
    private int precipitation;

    @JsonProperty("wind_speed")
    private int windSpeed;

    @Column(length = 50)
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    @JsonProperty("last_updated")//в якому вигляді буде вивід на екран цього поля
    private String lastUpdated;

    @OneToOne
    @JoinColumn(name="location_code")
    @MapsId
    @JsonIgnore
    private Location location;

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
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

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static String setTime () {
        LocalDateTime localDateTimeToSave = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String text = localDateTimeToSave.format(formatter);
        return text;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.locationCode = location.getCode();
        this.location = location;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RealtimeWeather other = (RealtimeWeather) obj;
        return Objects.equals(locationCode, other.locationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(locationCode);
    }

    public boolean equalsRealtimeWeather(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealtimeWeather that = (RealtimeWeather) o;
        return temperature == that.temperature && humidity == that.humidity && precipitation == that.precipitation && windSpeed == that.windSpeed && Objects.equals(locationCode, that.locationCode) && Objects.equals(status, that.status) && Objects.equals(lastUpdated, that.lastUpdated) && Objects.equals(location, that.location);
    }

    public int hashCodeRealtimeWeather() {
        return Objects.hash(locationCode, temperature, humidity, precipitation, windSpeed, status, lastUpdated, location);
    }

    @Override
    public String toString() {
        return "RealtimeWeather{" +
                "locationCode='" + locationCode + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", precipitation=" + precipitation +
                ", windSpeed=" + windSpeed +
                ", status='" + status + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", location=" + location +
                '}';
    }
}
