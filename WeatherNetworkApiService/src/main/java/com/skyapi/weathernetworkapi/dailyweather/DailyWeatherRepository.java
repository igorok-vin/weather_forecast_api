package com.skyapi.weathernetworkapi.dailyweather;

import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeatherId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DailyWeatherRepository extends CrudRepository<DailyWeather, DailyWeatherId> {
    @Query("""
            SELECT dw FROM DailyWeather dw WHERE dw.id.location.code =?1 and dw.id.location.trashed = false""")
    List<DailyWeather> findByLocationCode(String locationCode);

}
