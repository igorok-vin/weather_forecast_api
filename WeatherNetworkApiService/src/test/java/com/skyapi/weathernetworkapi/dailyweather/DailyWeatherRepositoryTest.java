package com.skyapi.weathernetworkapi.dailyweather;

import com.skyapi.weathernetworkapi.SecurityConfigurationDataJPATests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeatherId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfigurationDataJPATests.class)
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DailyWeatherRepositoryTest {

    @Autowired
    private DailyWeatherRepository dailyWeatherRepository;

    @Test
    public void testAddDailyWeather() {
      String locationCode = "PARIS_FR";
      Location location = new Location();
      location.setCode(locationCode);
        DailyWeather dailyWeather = new DailyWeather()
                .location(location)
                .dayOfMonth(5)
                .month(8)
                .minTemperature(20)
                .maxTemperature(23)
                .precipitation(45)
                .status("Cloudy");

        DailyWeather saved = dailyWeatherRepository.save(dailyWeather);
        assertThat(saved).isNotNull();
        assertThat(saved.getId().getLocation().getCode()).isEqualTo(locationCode);

    }

    @Test
    public void testADeleteDailyWeather() {
        Location location = new Location();
        location.setCode("PARIS_FR");
        DailyWeatherId dailyWeatherId = new DailyWeatherId(6,8,location);
        dailyWeatherRepository.deleteById(dailyWeatherId);

        assertThat(dailyWeatherRepository.existsById(dailyWeatherId)).isFalse();
    }

    @Test
    public void testFindHourlyWeatherByLocationCodeSuccess() {
        String locationCode = "DELHI_IN";

        List<DailyWeather> dailyWeathers = dailyWeatherRepository.findByLocationCode(locationCode);
        assertThat(dailyWeathers.size()).isEqualTo(2);
    }

    @Test
    public void testFindHourlyWeatherByLocationCodeNotFound() {
        String locationCode = "DELHI";

        List<DailyWeather> dailyWeathers = dailyWeatherRepository.findByLocationCode(locationCode);
        assertThat(dailyWeathers.size()).isEqualTo(0);
    }
}
