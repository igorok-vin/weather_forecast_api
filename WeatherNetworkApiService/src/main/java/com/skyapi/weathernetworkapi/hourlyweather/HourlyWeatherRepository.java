package com.skyapi.weathernetworkapi.hourlyweather;

import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeatherId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HourlyWeatherRepository extends CrudRepository<HourlyWeather, HourlyWeatherId> {

    @Query("""
            SELECT h FROM HourlyWeather h WHERE h.id.location.code = ?1 AND h.id.hourOfDay > ?2 AND h.id.location.trashed=false
            """)
    public List<HourlyWeather> findHourlyWeatherByLocationCodeAndCurrentHour(String locationCode, int currentHour);

    @Query("""
            SELECT h FROM HourlyWeather h WHERE h.id.location.code = ?1 AND h.id.location.trashed=false
            """)
    public List<HourlyWeather> findHourlyWeatherByLocationCode(String locationCode);
}

