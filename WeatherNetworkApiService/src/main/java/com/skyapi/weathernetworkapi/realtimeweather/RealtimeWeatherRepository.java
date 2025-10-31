package com.skyapi.weathernetworkapi.realtimeweather;

import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RealtimeWeatherRepository extends CrudRepository<RealtimeWeather, String> {

    @Query("SELECT rw FROM RealtimeWeather rw WHERE rw.location.countryCode = ?1 AND rw.location.cityName = ?2")
    public RealtimeWeather findByCountryCodeAndCityName(String countryCode, String cityName);

    @Query("SELECT rw FROM RealtimeWeather rw WHERE rw.locationCode = ?1 AND rw.location.trashed = false")
    public RealtimeWeather findByLocationCode(String locationCode);
}
