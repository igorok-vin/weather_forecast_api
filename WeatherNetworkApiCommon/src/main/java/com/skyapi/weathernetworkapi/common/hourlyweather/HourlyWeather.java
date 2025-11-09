package com.skyapi.weathernetworkapi.common.hourlyweather;

import com.skyapi.weathernetworkapi.common.Location;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "weather_hourly")
public class HourlyWeather implements Serializable {

    @EmbeddedId
    private HourlyWeatherId id = new HourlyWeatherId();

    @Range(min = -50, max = 50,message = "Temperature must be in the range of -50 to 50")
    private int temperature;

    @Range(min = 0, max = 100,message = "Precipitation must be in the range of 0 to 100")
    private int precipitation;

    @Column(length = 50)
    @NotBlank(message = "Status must no be empty")
    @Length(min = 3, max = 50, message="Status must be between 3-50 characters")
    private String status;

    public HourlyWeatherId getId() {
        return id;
    }

    public void setId(HourlyWeatherId id) {
        this.id = id;
    }

    @Range(min = -50, max = 50, message = "Temperature must be in the range of -50 to 50")
    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(@Range(min = -50, max = 50, message = "Temperature must be in the range of -50 to 50") int temperature) {
        this.temperature = temperature;
    }

    @Range(min = 0, max = 100, message = "Precipitation must be in the range of 0 to 100")
    public int getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(@Range(min = 0, max = 100, message = "Precipitation must be in the range of 0 to 100") int precipitation) {
        this.precipitation = precipitation;
    }

    public @NotBlank(message = "Status must no be empty") @Length(min = 3, max = 50, message = "Status must be between 3-50 characters") String getStatus() {
        return status;
    }

    public void setStatus(@NotBlank(message = "Status must no be empty") @Length(min = 3, max = 50, message = "Status must be between 3-50 characters") String status) {
        this.status = status;
    }

    public HourlyWeather temperature(int temperature) {
        setTemperature(temperature);
        return this;
    }

    public HourlyWeather id(Location location, int hour) {
        this.id.setLocation(location);
        this.id.setHourOfDay(hour);
        return this;
    }

    public HourlyWeather precipitation(int precipitation) {
        setPrecipitation(precipitation);
        return this;
    }

    public HourlyWeather status(String status) {
        setStatus(status);
        return this;
    }

    public HourlyWeather location(Location location) {
        this.id.setLocation(location);
        return this;
    }

    public HourlyWeather hourOfDay(int hour) {
        this.id.setHourOfDay(hour);
        return this;
    }

    @Override
    public String toString() {
        return "HourlyWeather{" +
                "hourOfDay=" + id.getHourOfDay() +
                ", temperature=" + temperature +
                ", precipitation=" + precipitation +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HourlyWeather other = (HourlyWeather) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public HourlyWeather getShallowCopy() {
        HourlyWeather shallowCopy = new HourlyWeather();
        shallowCopy.setId(this.getId());
        return shallowCopy;
    }
}
