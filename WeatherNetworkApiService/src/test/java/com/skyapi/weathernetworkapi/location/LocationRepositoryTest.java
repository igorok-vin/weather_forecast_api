package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.SecurityConfigurationDataJPATests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfigurationDataJPATests.class)
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LocationRepositoryTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testAddSuccess() {
        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        Location savedLocation = locationRepository.save(location);

        assertThat(savedLocation.getCode()).isEqualTo("NYC_USA");
        assertThat(savedLocation).isNotNull();
    }

    @Test
    @Disabled
    public void testListSuccess() {
        List<Location> locations = locationRepository.findAllUntrashed();

        assertThat(locations).isNotEmpty();
        locations.forEach(System.out::println);
    }

    @Test
    public void testListFirstPageSuccess() {
        int pageSize = 5;
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Location> page = locationRepository.findAllUntrashed(pageable);

        assertThat(page).size().isEqualTo(pageSize);
        page.forEach(System.out::println);
    }

    @Test
    public void testListPageNoContent() {
        int pageSize = 5;
        int pageNumber = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Location> page = locationRepository.findAllUntrashed(pageable);

        assertThat(page).isEmpty();
    }

    @Test
    public void testListSecondPageWithSortSuccess() {
        int pageSize = 5;
        int pageNumber = 0;
        Sort sort = Sort.by(Sort.Direction.DESC, "code");
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Location> page = locationRepository.findAllUntrashed(pageable);

        assertThat(page).size().isEqualTo(pageSize);
        page.forEach(System.out::println);
    }

    @Test
    public void testGetByCodeNotFound() {
        String code = "ADNC";
        Location location = locationRepository.findByCode(code);
        assertThat(location).isNull();
    }

    @Test
    public void testGetByCodeSuccess() {
        String code = "DELHI_IN";
        Location location = locationRepository.findByCode(code);
        assertThat(location).isNotNull();
        assertThat(location.getCode()).isEqualTo(code);
    }

    @Test
    public void testTrushedLocationSuccess() {
        String code = "NYC_USA";
        locationRepository.trashedByCode(code);
        Location location = locationRepository.findByCode(code);
        assertThat(location).isNull();
    }

    @Test
    public void testAddRealTimeWeatherData() {
        String code = "DELHI_IN";
        Location location = locationRepository.findByCode(code);
        RealtimeWeather realtimeWeather = new RealtimeWeather();
        Optional<RealtimeWeather> weather = Optional.ofNullable(location.getRealtimeWeather());
        if (weather.isEmpty()) {
            realtimeWeather.setLocation(location);
            location.setRealtimeWeather(realtimeWeather);
        }
        entityManager.persist(location);
        entityManager.flush();

        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(35);
        realtimeWeather.setHumidity(75);
        realtimeWeather.setPrecipitation(95);
        realtimeWeather.setStatus("Raining");
        realtimeWeather.setWindSpeed(35);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        Location updatedLocation = locationRepository.save(location);
        assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo(code);
    }

    @Test
    public void testAddHourlyWeatherData() {
        Location location = locationRepository.findById("DELHI_IN").get();
        List<HourlyWeather> listHourlyWeather = location.getListHourlyWeather();

        HourlyWeather hourlyWeather1 = new HourlyWeather().id(location, 8).temperature(23).precipitation(75).status("Cloudy");
        HourlyWeather hourlyWeather2 = new HourlyWeather().location(location).hourOfDay(9).temperature(21).precipitation(95).status("Raining");

        listHourlyWeather.add(hourlyWeather1);
        listHourlyWeather.add(hourlyWeather2);

        Location updatedLocation = locationRepository.save(location);

        assertThat(updatedLocation.getListHourlyWeather().size()).isEqualTo(2);
    }

    @Test
    public void testFindLocationByCountryCodeAndCityNameNotFound() {
        String codeCountry = "USSS";
        String cityName = "New York";
        Location location = locationRepository.findByCountryCodeAndCityName(codeCountry, cityName);

        assertThat(location).isNull();
    }

    @Test
    public void testFindLocationByCountryCodeAndCityNameSuccess() {
        String codeCountry = "US";
        String cityName = "New York City";
        Location location = locationRepository.findByCountryCodeAndCityName(codeCountry, cityName);

        assertThat(location).isNotNull();
        assertThat(location.getCountryCode()).isEqualTo(codeCountry);
        assertThat(location.getCityName()).isEqualTo(cityName);
    }

    @Test
    public void testAddDailyWeatherData() {
        Location location = locationRepository.findById("DELHI_IN").get();
        List<DailyWeather> listDailyWeather = location.getListDailyWeather();

        DailyWeather dailyWeather1 = new DailyWeather()
                .location(location)
                .dayOfMonth(16)
                .month(7)
                .minTemperature(25)
                .maxTemperature(33)
                .precipitation(20)
                .status("Sunny");

        DailyWeather dailyWeather2 = new DailyWeather()
                .location(location)
                .dayOfMonth(17)
                .month(7)
                .minTemperature(26)
                .maxTemperature(34)
                .precipitation(10)
                .status("Clear");

        listDailyWeather.add(dailyWeather1);
        listDailyWeather.add(dailyWeather2);
        Location savedLocation = locationRepository.save(location);
        assertThat(savedLocation.getListDailyWeather()).isNotEmpty();
    }

    @Test
    public void testFindLocationByCountryCodeAndCityNameSuccess2() {
        String codeCountry = "ES";
        String cityName = "Madrid";
        Location location = locationRepository.findByCountryCodeAndCityName(codeCountry, cityName);

        assertThat(location).isNotNull();
        assertThat(location.getCountryCode()).isEqualTo(codeCountry);
        assertThat(location.getCityName()).isEqualTo(cityName);
    }
}
