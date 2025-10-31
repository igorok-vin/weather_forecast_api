package com.skyapi.weathernetworkapi.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "locations")
@JsonPropertyOrder({"code", "city_name", "region_name", "country_code", "country_name", "enabled"})
public class Location {

    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String code;

    @Column(length = 128, nullable = false)
    @JsonProperty("city_name")
    private String cityName;

    @Column(length = 128)
    @JsonProperty("region_name")
    private String regionName;

    @Column(length = 64, nullable = false)
    @JsonProperty("country_name")
    private String countryName;

    @Column(length = 10, nullable = false)
    @JsonProperty("country_code")
    private String countryCode;

    private boolean enabled;

    @JsonIgnore
    private boolean trashed;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @JsonIgnore
    private RealtimeWeather realtimeWeather;

    @OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<HourlyWeather> listHourlyWeather = new ArrayList<>();

    @OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonIgnore
    private List<DailyWeather> listDailyWeather = new ArrayList<>();

    public Location() {
    }

    public Location(String cityName, String regionName, String countryName, String countryCode) {
        super();
        this.cityName = cityName;
        this.regionName = regionName;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public Location(String code, String cityName, String regionName, String countryName, String countryCode) {
        this(cityName, regionName, countryName, countryCode);
        this.code = code;
    }

    public Location(String code, String cityName, String regionName, String countryName, String countryCode, boolean enabled) {
        this.code = code;
        this.cityName = cityName;
        this.regionName = regionName;
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.enabled = enabled;
    }

    public Location(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        return Objects.equals(code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    public RealtimeWeather getRealtimeWeather() {
        return realtimeWeather;
    }

    public void setRealtimeWeather(RealtimeWeather realtimeWeather) {
        this.realtimeWeather = realtimeWeather;
    }

    @Override
    public String toString() {
        return code +" => " +  cityName + ", " + (regionName != null ? regionName + ", " : "")  + countryName;
    }

    public List<HourlyWeather> getListHourlyWeather() {
        return listHourlyWeather;
    }

    public void setListHourlyWeather(List<HourlyWeather> listHourlyWeather) {
        this.listHourlyWeather = listHourlyWeather;
    }

    public Location code(String code) {
      setCode(code);
      return this;
    }

    public List<DailyWeather> getListDailyWeather() {
        return listDailyWeather;
    }

    public void setListDailyWeather(List<DailyWeather> listDailyWeather) {
        this.listDailyWeather = listDailyWeather;
    }
}
