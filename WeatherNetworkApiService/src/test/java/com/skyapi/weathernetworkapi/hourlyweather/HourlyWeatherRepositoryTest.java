package com.skyapi.weathernetworkapi.hourlyweather;

import com.skyapi.weathernetworkapi.SecurityConfigurationDataJPATests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfigurationDataJPATests.class)
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HourlyWeatherRepositoryTest {

    @Autowired
    private HourlyWeatherRepository hourlyWeatherRepository;

    @Test
    public void testAddHourlyWeather() {
        String locationCode = "LACA_USA";
        Location location = new Location().code(locationCode);
        HourlyWeather hourlyWeather = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");

        HourlyWeather saved = hourlyWeatherRepository.save(hourlyWeather);
        assertThat(saved).isNotNull();
        assertThat(saved.getId().getLocation().getCode()).isEqualTo(locationCode);
        assertThat(saved.getId().getHourOfDay()).isEqualTo(13);
    }

    @Test
    public void testDeleteHourlyWeather() {
        Location location = new Location().code("LACA_USA");
        HourlyWeather hourlyWeather = new HourlyWeather().id(location, 13);
        hourlyWeatherRepository.delete(hourlyWeather);
        Optional<HourlyWeather> result = hourlyWeatherRepository.findById(hourlyWeather.getId());
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void testFindHourlyWeatherByLocationCodeSuccess() {
        String locationCode = "DELHI_IN";
        int currentHour = 8;
        List<HourlyWeather> hourlyWeatherList = hourlyWeatherRepository.findHourlyWeatherByLocationCodeAndCurrentHour(locationCode, currentHour);
        assertThat(hourlyWeatherList.size()).isEqualTo(1);
    }

    @Test
    public void testFindHourlyWeatherByLocationCodeNotFound() {
        String locationCode = "DELHI_IN";
        int currentHour = 20;
        List<HourlyWeather> hourlyWeatherList = hourlyWeatherRepository.findHourlyWeatherByLocationCodeAndCurrentHour(locationCode, currentHour);
        assertThat(hourlyWeatherList.size()).isEqualTo(0);
    }
}
