package com.skyapi.weathernetworkapi.fullweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.GeolocationService;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherDTO;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherDTO;
import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;
import org.hamcrest.CoreMatchers;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FullWeatherApiController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfigurationWebMvcTests.class)
@ActiveProfiles("permitAllRequestForTest")
@AutoConfigureMockMvc
public class FullWeatherApiControllerTest {

    private static final String END_POINT = "/v1/full";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FullWeatherService fullWeatherService;

    @MockitoBean
    private GeolocationService geolocationService;

   @Autowired
    private FullWeatherModelAssembler fullWeatherModelAssembler;


    @Test
    public void testGetByIpAddressShouldReturn404NotFoundBecauseGeolocationException() throws Exception {
        GeolocatioException exception = new GeolocatioException("Geolocation error");
        when(geolocationService.getLocation(anyString())).thenThrow(exception);

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(exception.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByIpAddressShouldReturn404NotFoundBecauseLocationNotFoundException() throws Exception {
        Location location = new Location().code("ABCD");
        when(geolocationService.getLocation(anyString())).thenReturn(location);

        LocationNotFoundException exception = new LocationNotFoundException(location.getCode());
        when(fullWeatherService.getLocationInDBByLocationFromIP(location)).thenThrow(exception);

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(exception.getMessage())))
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

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        location.setRealtimeWeather(realtimeWeather);

        HourlyWeather hourlyWeather1 = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");
        HourlyWeather hourlyWeather2 = new HourlyWeather().id(location, 14).temperature(28).precipitation(25).status("Sunny");
        HourlyWeather hourlyWeather3 = new HourlyWeather().id(location, 15).temperature(22).precipitation(80).status("Raining");

        location.setListHourlyWeather(List.of(hourlyWeather1, hourlyWeather2, hourlyWeather3));

        DailyWeather dailyWeather1 = new DailyWeather().id(location, 10, 5).minTemperature(20).maxTemperature(23).precipitation(25).status("Clear");
        DailyWeather dailyWeather2 = new DailyWeather().id(location, 11, 5).minTemperature(18).maxTemperature(20).precipitation(80).status("Rain");
        DailyWeather dailyWeather3 = new DailyWeather().id(location, 12, 5).minTemperature(27).maxTemperature(30).precipitation(60).status("Cloudy");

        location.setListDailyWeather(List.of(dailyWeather1, dailyWeather2, dailyWeather3));

        when(geolocationService.getLocation(anyString())).thenReturn(location);
        when(fullWeatherService.getLocationInDBByLocationFromIP(location)).thenReturn(location);

        String expectedLocation = location.toString();

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/HAL+json"))
                .andExpect(header().string("Cache-Control", CoreMatchers.containsString("max-age=1800")))
                .andExpect(jsonPath("$.location", CoreMatchers.is(expectedLocation)))
                .andExpect(jsonPath("$.location", CoreMatchers.is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[2].day_of_month", CoreMatchers.is(12)))
                .andExpect(jsonPath("$.daily_forecast[1].status", CoreMatchers.is("Rain")))
                .andExpect(jsonPath("$.daily_forecast[2].max_temperature", CoreMatchers.is(30)))
                .andExpect(jsonPath("$.hourly_forecast[0].temperature", CoreMatchers.is(27)))
                .andExpect(jsonPath("$.hourly_forecast[1].precipitation", CoreMatchers.is(25)))
                .andExpect(jsonPath("$.hourly_forecast[1].status", CoreMatchers.is("Sunny")))
                .andExpect(jsonPath("$.realtime_weather.status", CoreMatchers.is("Cloudy")))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByLocationCodeShouldReturn200OK() throws Exception {
        Location location = new Location();
        location.setCode("DELHI_IN");
        location.setCityName("Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        location.setRealtimeWeather(realtimeWeather);

        HourlyWeather hourlyWeather1 = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");
        HourlyWeather hourlyWeather2 = new HourlyWeather().id(location, 14).temperature(28).precipitation(25).status("Sunny");
        HourlyWeather hourlyWeather3 = new HourlyWeather().id(location, 15).temperature(22).precipitation(80).status("Raining");

        location.setListHourlyWeather(List.of(hourlyWeather1, hourlyWeather2, hourlyWeather3));

        DailyWeather dailyWeather1 = new DailyWeather().id(location, 10, 5).minTemperature(20).maxTemperature(23).precipitation(25).status("Clear");
        DailyWeather dailyWeather2 = new DailyWeather().id(location, 11, 5).minTemperature(18).maxTemperature(20).precipitation(80).status("Rain");
        DailyWeather dailyWeather3 = new DailyWeather().id(location, 12, 5).minTemperature(27).maxTemperature(30).precipitation(60).status("Cloudy");

        location.setListDailyWeather(List.of(dailyWeather1, dailyWeather2, dailyWeather3));

        when(fullWeatherService.getLocationByCode(location.getCode())).thenReturn(location);

        String expectedLocation = location.toString();

        mockMvc.perform(get(END_POINT +"/DELHI_IN"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.location", CoreMatchers.is(expectedLocation)))
                .andExpect(jsonPath("$.location", CoreMatchers.is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[2].day_of_month", CoreMatchers.is(12)))
                .andExpect(jsonPath("$.daily_forecast[1].status", CoreMatchers.is("Rain")))
                .andExpect(jsonPath("$.daily_forecast[2].max_temperature", CoreMatchers.is(30)))
                .andExpect(jsonPath("$.hourly_forecast[0].temperature", CoreMatchers.is(27)))
                .andExpect(jsonPath("$.hourly_forecast[1].precipitation", CoreMatchers.is(25)))
                .andExpect(jsonPath("$.hourly_forecast[1].status", CoreMatchers.is("Sunny")))
                .andExpect(jsonPath("$.realtime_weather.status", CoreMatchers.is("Cloudy")))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full/" + location.getCode())))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn404NotFoundBecauseLocationNotFoundException() throws Exception {
        Location location = new Location().code("ABCD");
        LocationNotFoundException exception = new LocationNotFoundException(location.getCode());
        when(fullWeatherService.getLocationByCode(anyString())).thenThrow(exception);

        mockMvc.perform(get(END_POINT + "/ABCD"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(exception.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoHourlyWeatherData() throws Exception {
       String locationCode = "NYC_USA";
       String requestURI = END_POINT + "/" + locationCode;
       
       FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

       String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Hourly weather data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoDailyWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(10).temperature(50).precipitation(10).status("Sunny");

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Daily weather data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidRealtimeWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(10).temperature(27).precipitation(10).status("Sunny");

        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO().dayOfMonth(1).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(225);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));
        fullWeatherDTO.setListDailyWeather(List.of(dailyWeatherDTO));
        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("Temperature must be in the range of -50 to 50")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidHourlyWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(10).temperature(17).precipitation(110).status("Sunny");

        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO().dayOfMonth(1).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));
        fullWeatherDTO.setListDailyWeather(List.of(dailyWeatherDTO));
        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", containsString("Precipitation must be in the range of 0 to 100")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidDailyWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(10).temperature(17).precipitation(10).status("Sunny");

        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO().dayOfMonth(32).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("");

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));
        fullWeatherDTO.setListDailyWeather(List.of(dailyWeatherDTO));
        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[?(@.errors =~ /.*Day of month must be in between 1-31*./)].errors").hasJsonPath())
                .andExpect(jsonPath("$[?(@.errors =~ /.*Status must be between 3-50 characters*./)].errors").hasJsonPath())
                .andExpect(jsonPath("$[?(@.errors =~ /.*Status must not be empty*./)].errors").hasJsonPath())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFoundBecauseLocationNotFoundException() throws Exception {
        String locationCode = "ANCH";
        String requestURI = END_POINT + "/" + locationCode;

        Location location = new Location();
        location.setCode("DELHI_IN");

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(10).temperature(17).precipitation(10).status("Sunny");

        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO().dayOfMonth(2).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Cloudy");

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));
        fullWeatherDTO.setListDailyWeather(List.of(dailyWeatherDTO));
        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        LocationNotFoundException exception = new LocationNotFoundException(locationCode);
        when(fullWeatherService.update(eq(locationCode),any())).thenThrow(exception);

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is(exception.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200Ok() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT + "/" + locationCode;

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States Of America");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setLocation(location);
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);
        realtimeWeather.setLastUpdated(RealtimeWeather.setTime());

        location.setRealtimeWeather(realtimeWeather);

        HourlyWeather hourlyWeather = new HourlyWeather().id(location, 13).temperature(27).precipitation(10).status("Sunny");

        location.setListHourlyWeather(List.of(hourlyWeather));

        DailyWeather dailyWeather = new DailyWeather().id(location, 10, 5).minTemperature(20).maxTemperature(23).precipitation(25).status("Clear");

        location.setListDailyWeather(List.of(dailyWeather));

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO().hourOfDay(13).temperature(27).precipitation(10).status("Sunny");

        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO().dayOfMonth(10).month(5).min_temperature(20).max_temperature(23).precipitation(23).status("Clear");

        RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
        realtimeWeatherDTO.setTemperature(25);
        realtimeWeatherDTO.setHumidity(52);
        realtimeWeatherDTO.setPrecipitation(15);
        realtimeWeatherDTO.setStatus("Cloudy");
        realtimeWeatherDTO.setWindSpeed(10);
        realtimeWeatherDTO.setLastUpdated(RealtimeWeather.setTime());

        fullWeatherDTO.setListHourlyWeather(List.of(hourlyWeatherDTO));
        fullWeatherDTO.setListDailyWeather(List.of(dailyWeatherDTO));
        fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        when(fullWeatherService.update(eq(locationCode),any())).thenReturn(location);
        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/HAL+json"))
                .andExpect(jsonPath("$.realtime_weather.temperature", CoreMatchers.is(25)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", CoreMatchers.is(13)))
                .andExpect(jsonPath("$.daily_forecast[0].precipitation", CoreMatchers.is(25)))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full/" + location.getCode())))
                .andDo(print());
    }

}
