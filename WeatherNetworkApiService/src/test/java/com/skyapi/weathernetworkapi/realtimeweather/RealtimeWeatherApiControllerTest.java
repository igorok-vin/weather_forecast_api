package com.skyapi.weathernetworkapi.realtimeweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RealtimeWeatherApiController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfigurationWebMvcTests.class)
@ActiveProfiles("permitAllRequestForTest")
@AutoConfigureMockMvc
public class RealtimeWeatherApiControllerTest {
    private static final String END_POINT_PATH = "/v1/realtime";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RealtimeWeatherService realtimeWeatherService;

    @MockitoBean
    private GeolocationService geolocationService;

    @Test
    public void testGetShouldReturnStatus404NotFoundBecauseGeolocationException() throws Exception {
        when((geolocationService.getLocation(any(String.class)))).thenThrow(GeolocatioException.class);
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus404NotFoundBecauseRealtimeWeatherNotFoundException() throws Exception {
        Location location = new Location();
        when((geolocationService.getLocation(any(String.class)))).thenReturn(location);
        when(realtimeWeatherService.getByLocation(location)).thenThrow(RealtimeWeatherNotFoundException.class);
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus200OK() throws Exception {
        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();

        location.setRealtimeWeather(realtimeWeather);
        when((geolocationService.getLocation(any(String.class)))).thenReturn(location);
        when(realtimeWeatherService.getByLocation(location)).thenReturn(realtimeWeather);
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().string("Cache-Control", containsString("max-age=1800")))
                .andExpect(header().exists("Last-Modified"))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeReturnStatus404NotFound() throws Exception {
        when((realtimeWeatherService.getRealtimeWeatherByLocationCode(any(String.class)))).thenThrow(RealtimeWeatherNotFoundException.class);
        mockMvc.perform(get(END_POINT_PATH + "/abc"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeReturnStatus200OK() throws Exception {
        String locationCode = "NYC_USA";

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        String expectedLocation = location.getCode() + " => " + location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();

        when(realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode)).thenReturn(realtimeWeather);
        mockMvc.perform(get(END_POINT_PATH + "/NYC_USA"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().exists("Last-Modified"))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus400BadRequest() throws Exception {
        String locationCode = "1USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(200);
        realtimeWeather.setHumidity(110);
        realtimeWeather.setPrecipitation(210);
        realtimeWeather.setStatus("aj");
        realtimeWeather.setWindSpeed(320);

        String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "1_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
        dto.setTemperature(20);
        dto.setHumidity(10);
        dto.setPrecipitation(10);
        dto.setStatus("Cloudy");
        dto.setWindSpeed(30);

        when(realtimeWeatherService.update(eq(locationCode), any())).thenThrow(LocationNotFoundException.class);

        String bodyContent = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(20);
        realtimeWeather.setHumidity(10);
        realtimeWeather.setPrecipitation(10);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(30);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        location.setRealtimeWeather(realtimeWeather);

        when(realtimeWeatherService.update(locationCode, realtimeWeather)).thenReturn(realtimeWeather);

        String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }
}
