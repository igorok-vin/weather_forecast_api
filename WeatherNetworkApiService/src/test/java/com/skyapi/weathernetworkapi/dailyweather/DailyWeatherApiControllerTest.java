package com.skyapi.weathernetworkapi.dailyweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DailyWeatherApiController.class)
@Import(SecurityConfigurationWebMvcTests.class)
@ActiveProfiles("permitAllRequestForTest")
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class DailyWeatherApiControllerTest {
    private static final String END_POINT = "/v1/daily";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DailyWeatherService dailyWeatherService;

    @MockitoBean
    private GeolocationService geolocationService;

    @Test
    public void testGetByIpAddressShouldReturn404NotFoundBecauseGeolocationException() throws Exception {
        when(geolocationService.getLocation(anyString())).thenThrow(GeolocatioException.class);
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetByIpAddressShouldReturn204NoContent() throws Exception {
        Location location = new Location().code("DELHI_IN");

        when(geolocationService.getLocation(anyString())).thenReturn(location);
        when(dailyWeatherService.getDailyWeatherByLocation(location)).thenReturn(new ArrayList<>());

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByIpAddressShouldReturn200OK() throws Exception {
        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        DailyWeather dailyWeather1 = new DailyWeather().id(location, 10, 5).minTemperature(20).maxTemperature(23).precipitation(25).status("Clear");
        DailyWeather dailyWeather2 = new DailyWeather().id(location, 11, 5).minTemperature(18).maxTemperature(20).precipitation(80).status("Rain");
        DailyWeather dailyWeather3 = new DailyWeather().id(location, 12, 5).minTemperature(27).maxTemperature(30).precipitation(60).status("Cloudy");

        when(geolocationService.getLocation(anyString())).thenReturn(location);
        when(dailyWeatherService.getDailyWeatherByLocation(location)).thenReturn(List.of(dailyWeather1, dailyWeather2, dailyWeather3));

        String expectedLocation = location.toString();
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().string("Cache-Control",containsString("max-age=21600")))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(10)))
                .andExpect(jsonPath("$.daily_forecast[2].day_of_month", is(12)))
                .andExpect(jsonPath("$.daily_forecast[1].status", is("Rain")))
                .andExpect(jsonPath("$.daily_forecast[2].max_temperature", is(30)))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetDailyWeatherByLocationCodeShouldReturn200OK() throws Exception {
        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        DailyWeather dailyWeather1 = new DailyWeather().id(location, 10, 5).minTemperature(20).maxTemperature(23).precipitation(25).status("Clear");
        DailyWeather dailyWeather2 = new DailyWeather().id(location, 11, 5).minTemperature(18).maxTemperature(20).precipitation(80).status("Rain");
        DailyWeather dailyWeather3 = new DailyWeather().id(location, 12, 5).minTemperature(27).maxTemperature(30).precipitation(60).status("Cloudy");

        when(dailyWeatherService.getDailyWeatherByLocationCode(location.getCode())).thenReturn(List.of(dailyWeather1, dailyWeather2, dailyWeather3));

        String expectedLocation = location.toString();
        mockMvc.perform(get(END_POINT + "/DELHI_IN"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control",containsString("max-age=21600")))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(10)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily/" + location.getCode())))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + location.getCode())))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + location.getCode())))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + location.getCode())))
                .andDo(print());
    }

    @Test
    public void testGetDailyWeatherByLocationCodeShouldReturn204NoContent() throws Exception {
        Location location = new Location().code("DELHI_IN");

        when(dailyWeatherService.getDailyWeatherByLocationCode(location.getCode())).thenReturn(new ArrayList<>());
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetDailyWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
        String locationCode = "LACA_US";
        String requestURI = END_POINT + "/" + locationCode;
        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        doThrow(ex).when(dailyWeatherService).getDailyWeatherByLocationCode(anyString());

        mockMvc.perform(get(requestURI))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateDailyWeatherByLocationCodeShouldReturn400BadRequestBecauseNoDataFound() throws Exception {
        String requestURI = END_POINT + "/DELHI_IN";
        List<DailyWeatherDTO> weatherDTOS = Collections.emptyList();

        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Daily forecast data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn400BadRequestBecauseInvalidData() throws Exception {
        String requestURI = END_POINT +"/DELHI_IN";

        DailyWeatherDTO forecast1 = new DailyWeatherDTO().dayOfMonth(-1).month(-7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");
        DailyWeatherDTO forecast2 = new DailyWeatherDTO().dayOfMonth(2).month(7).min_temperature(125).max_temperature(127).precipitation(60).status("Cloudy");
        DailyWeatherDTO forecast3 = new DailyWeatherDTO().dayOfMonth(3).month(7).min_temperature(20).max_temperature(23).precipitation(190).status("");

        List<DailyWeatherDTO> weatherDTOS = List.of(forecast1,forecast2,forecast3);

        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Temperature must be in the range of -50 to 50*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Precipitation must be in the range of 0 to 100*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Day of month must be in between 1-31*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Month must be in the range of 1 to 12*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Status must be between 3-50 characters*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Status must not be empty*./)].errors").hasJsonPath())
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn200Ok() throws Exception {
        String locationCode = "NYC_USA";

        String requestURI = END_POINT +"/" + locationCode;

        DailyWeatherDTO forecast1 = new DailyWeatherDTO().dayOfMonth(1).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");
        DailyWeatherDTO forecast2 = new DailyWeatherDTO().dayOfMonth(2).month(7).min_temperature(30).max_temperature(33).precipitation(60).status("Cloudy");

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States of America");

        DailyWeather changedForecast1 = new DailyWeather().location(location).dayOfMonth(1).month(7).minTemperature(25).maxTemperature(27).precipitation(25).status("Sunny");

        DailyWeather changedForecast2 = new DailyWeather().location(location).dayOfMonth(2).month(7).minTemperature(21).maxTemperature(23).precipitation(90).status("Rain");

        List<DailyWeatherDTO> forecastList = List.of(forecast1,forecast2);
        List<DailyWeather> changedForecastList = List.of(changedForecast1,changedForecast2);

        /* use the object mapper to convert this weatherDTOS to a JSON string.*/
        String requestBody = objectMapper.writeValueAsString(forecastList);

        when(dailyWeatherService.updateDailyWeatherByLocationCode(eq(locationCode),anyList())).thenReturn(changedForecastList);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is(location.toString())))
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(1)))
                .andExpect(jsonPath("$.daily_forecast[0].status", is("Sunny")))
                .andExpect(jsonPath("$.daily_forecast[1].day_of_month", is(2)))
                .andExpect(jsonPath("$.daily_forecast[1].precipitation", is(90)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

}
