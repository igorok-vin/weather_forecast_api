package com.skyapi.weathernetworkapi.realtimeweather;

import com.skyapi.weathernetworkapi.SecurityConfigurationDataJPATests;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfigurationDataJPATests.class)
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RealtimeWeatherRepositoryTest {

    @Autowired
    private RealtimeWeatherRepository realtimeWeatherRepository;

    @Test
    public void testUpdateRealtimeWeather() {
        String locationCode = "NYC_USA";
        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findById(locationCode).get();

        realtimeWeather.setTemperature(-20);
        realtimeWeather.setHumidity(60);
        realtimeWeather.setPrecipitation(100);
        realtimeWeather.setStatus("Snowing");
        realtimeWeather.setWindSpeed(18);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        RealtimeWeather savedRealtimeWeather = realtimeWeatherRepository.save(realtimeWeather);
        assertThat(savedRealtimeWeather.getTemperature()).isEqualTo(-20);
    }

    @Test
    public void testFindRealtimeWeatherByCountryCodeAndCityNameNotFound() {
        String countryCode = "JP";
        String cityName = "Tokyo";

        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode, cityName);

        assertThat(realtimeWeather).isNull();
    }

    @Test
    public void testFindRealtimeWeatherByCountryCodeAndCityName() {
        String countryCode = "US";
        String cityName = "New York City";

        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode, cityName);

        assertThat(realtimeWeather).isNotNull();
        assertThat(realtimeWeather.getLocation().getCityName()).isEqualTo("New York City");
    }

    @Test
    public void testFindRealtimeWeatherByLocationCode() {
        String locationCode = "NYC_USA";
        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByLocationCode(locationCode);
        assertThat(realtimeWeather).isNotNull();
        assertThat(realtimeWeather.getLocation().getCityName()).isEqualTo("New York City");
    }

    @Test
    public void testFindRealtimeWeatherByTrashedLocationCodeNotFound() {
        String locationCode = "LACA_USA";
        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByLocationCode(locationCode);
        assertThat(realtimeWeather).isNull();
    }

    @Test
    public void testFindByLocationNotFound() {
        String locationCode = "ABZ";
        RealtimeWeather realtimeWeather = realtimeWeatherRepository.findByLocationCode(locationCode);

        assertThat(realtimeWeather).isNull();
    }
}
