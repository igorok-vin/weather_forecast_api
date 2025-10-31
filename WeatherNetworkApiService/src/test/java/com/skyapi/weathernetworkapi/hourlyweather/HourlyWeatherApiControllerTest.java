package com.skyapi.weathernetworkapi.hourlyweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
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

import static javax.management.Query.value;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(HourlyWeatherApiController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfigurationWebMvcTests.class)
@ActiveProfiles({"permitAllRequestForTest"})
@AutoConfigureMockMvc
public class HourlyWeatherApiControllerTest {
    private static final String END_POINT = "/v1/hourly";
    public static final String X_CURRENT_HOUR = "X-Current-Hour";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HourlyWeatherService hourlyWeatherService;

    @MockitoBean
    private GeolocationService geolocationService;

    @Test
    public void testGetByIpAddressShouldReturn400BadRequestBecauseNoHeaderXCurrentHour() throws Exception {
        mockMvc.perform(get(END_POINT))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByIpAddressShouldReturn404NotFoundBecauseGeolocationException() throws Exception {
        when(geolocationService.getLocation(anyString())).thenThrow(GeolocatioException.class);
        mockMvc.perform(get(END_POINT).header(X_CURRENT_HOUR, "5"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetByIpAddressShouldReturn204NoContent() throws Exception {
        int currentHour = 5;
        Location location = new Location().code("DELHI_IN");

        when(geolocationService.getLocation(anyString())).thenReturn(location);
        when(hourlyWeatherService.getAllHourlyWeatherByLocationBasedOnIPAddress(location,currentHour)).thenReturn(new ArrayList<>());

        mockMvc.perform(get(END_POINT).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByIpAddressShouldReturn200OK() throws Exception {
        int currentHour = 5;
        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");
        HourlyWeather forecast2 = new HourlyWeather().id(location, 14).temperature(28).precipitation(0).status("Sunny");
        HourlyWeather forecast3 = new HourlyWeather().id(location, 15).temperature(22).precipitation(80).status("Raining");


        when(geolocationService.getLocation(anyString())).thenReturn(location);
        when(hourlyWeatherService.getAllHourlyWeatherByLocationBasedOnIPAddress(location,currentHour)).thenReturn(List.of(forecast1, forecast2, forecast3));

        String expectedLocation = location.toString();
        mockMvc.perform(get(END_POINT).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")))
                .andExpect(jsonPath("$.location",is(expectedLocation)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day",is(13)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByLocationCodeShouldReturn200OK() throws Exception {
        int currentHour = 12;
        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");
        HourlyWeather forecast2 = new HourlyWeather().id(location, 14).temperature(28).precipitation(0).status("Sunny");
        HourlyWeather forecast3 = new HourlyWeather().id(location, 15).temperature(22).precipitation(80).status("Raining");

        when(hourlyWeatherService.getAllHourlyWeatherByLocationCodeAndCurrentHour(location.getCode(),currentHour)).thenReturn(List.of(forecast1, forecast2, forecast3));

        String expectedLocation = location.toString();
        mockMvc.perform(get(END_POINT + "/DELHI_IN").header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().string("Cache-Control", containsString("max-age=3600")))
                .andExpect(jsonPath("$.location",is(expectedLocation)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day",is(13)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly" + "/" + location.getCode())))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime"+ "/" + location.getCode())))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily"+ "/" + location.getCode())))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full"+ "/" + location.getCode())))
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByLocationCodeShouldReturn204NoContent() throws Exception {
        int currentHour = 5;
        Location location = new Location().code("DELHI_IN");

        when(hourlyWeatherService.getAllHourlyWeatherByLocationCodeAndCurrentHour(location.getCode(),currentHour)).thenReturn(new ArrayList<>());

        mockMvc.perform(get(END_POINT+"/DELHI_IN").header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByLocationCodeShouldReturn400BadRequest() throws Exception {
        int currentHour = 5;
        Location location = new Location().code("DELHI_IN");

        when(hourlyWeatherService.getAllHourlyWeatherByLocationCodeAndCurrentHour(location.getCode(),currentHour)).thenReturn(new ArrayList<>());

        mockMvc.perform(get(END_POINT+"/DELHI_IN"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn400BadRequestBecauseNoDataFound() throws Exception {
        String requestURI = END_POINT +"/DELHI_IN";

        List<HourlyWeatherDTO> weatherDTOS = Collections.emptyList();
        /* use the object mapper to convert this weatherDTOS to a JSON string.*/
        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]",is("Hourly forecast data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn400BadRequestBecauseInvalidData() throws Exception {
        String requestURI = END_POINT +"/DELHI_IN";

        HourlyWeatherDTO forecast1 = new HourlyWeatherDTO().hourOfDay(10).temperature(127).precipitation(10).status("Sunny");
        HourlyWeatherDTO forecast2 = new HourlyWeatherDTO().hourOfDay(-1).temperature(28).precipitation(500).status("Sunny");
        HourlyWeatherDTO forecast3 = new HourlyWeatherDTO().hourOfDay(12).temperature(22).precipitation(80).status("");

        List<HourlyWeatherDTO> weatherDTOS = List.of(forecast1,forecast2,forecast3);

        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Temperature must be in the range of -50 to 50*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Precipitation must be in the range of 0 to 100*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Hour of day must be in between 0-23*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Status must be between 3-50 characters*./)].errors").hasJsonPath())
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
        String locationCode = "DELHI_ABC";
        String requestURI = END_POINT +"/" + locationCode;

        HourlyWeatherDTO forecast1 = new HourlyWeatherDTO().hourOfDay(10).temperature(12).precipitation(10).status("Sunny");

        List<HourlyWeatherDTO> weatherDTOS = new ArrayList<>();
        weatherDTOS.addAll(List.of(forecast1));

        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        when(hourlyWeatherService.updateHourlyWeatherByLocationCode(anyString(), anyList())).thenThrow(LocationNotFoundException.class);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testUpdateHourlyWeatherByLocationCodeShouldReturn200OK() throws Exception {
        String locationCode = "DELHI_IN";
        String requestURI = END_POINT +"/" + locationCode;

        HourlyWeatherDTO forecastDTO1 = new HourlyWeatherDTO().hourOfDay(10).temperature(12).precipitation(10).status("Sunny");

        HourlyWeatherDTO forecastDTO2 = new HourlyWeatherDTO().hourOfDay(11).temperature(28).precipitation(80).status("Raining");

        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather().id(location, 10).temperature(12).precipitation(10).status("Sunny");
        HourlyWeather forecast2 = new HourlyWeather().id(location, 11).temperature(28).precipitation(80).status("Raining");

        List<HourlyWeatherDTO> weatherDTOS = new ArrayList<>();
        weatherDTOS.addAll(List.of(forecastDTO1,forecastDTO2));

        var hourlyForecast = List.of(forecast1,forecast2);

        String requestBody = objectMapper.writeValueAsString(weatherDTOS);

        when(hourlyWeatherService.updateHourlyWeatherByLocationCode(anyString(), anyList())).thenReturn(hourlyForecast);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.location",is(location.toString())))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day",is(10)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly" + "/" + location.getCode())))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime"+ "/" + location.getCode())))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily"+ "/" + location.getCode())))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full"+ "/" + location.getCode())))
                .andDo(print());
    }

}
