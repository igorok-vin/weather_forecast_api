package com.skyapi.weathernetworkapi.fullweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherDTO;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherDTO;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles({"testing"/*,"permitAllRequestForTest"*/})
@Sql(scripts = "classpath:drop.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/data-test.sql"},config = @SqlConfig(separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR))
@AutoConfigureMockMvc

public class FullWeatherIntegrationTest {
    private static final String END_POINT_PATH = "/v1/full";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGeFullWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is("No location found with the given location code: AJFLD")))
                .andDo(print());
    }

    @Test
    public void testGeFullWeatherByIPAddressShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For", "000.198"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]", is("No location found with the given country code - and city name: -")))
                .andDo(print());
    }

    @Test
    public void testFullWeatherByApiAddressShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","103.48.198.141"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is("DELHI_IN => Delhi, Delhi, India")))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[2].day_of_month", CoreMatchers.is(12)))
                .andExpect(jsonPath("$.daily_forecast[1].status", CoreMatchers.is("Cloudy")))
                .andExpect(jsonPath("$.daily_forecast[2].max_temperature", CoreMatchers.is(23)))
                .andExpect(jsonPath("$.hourly_forecast[0].temperature", CoreMatchers.is(23)))
                .andExpect(jsonPath("$.hourly_forecast[1].precipitation", CoreMatchers.is(95)))
                .andExpect(jsonPath("$.hourly_forecast[1].status", CoreMatchers.is("Raining")))
                .andExpect(jsonPath("$.realtime_weather.status", CoreMatchers.is("Raining")))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testFullWeatherByLocationCodeShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH + "/DELHI_IN";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.location", is("DELHI_IN => Delhi, Delhi, India")))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", CoreMatchers.is(10)))
                .andExpect(jsonPath("$.daily_forecast[2].day_of_month", CoreMatchers.is(12)))
                .andExpect(jsonPath("$.daily_forecast[1].status", CoreMatchers.is("Cloudy")))
                .andExpect(jsonPath("$.daily_forecast[2].max_temperature", CoreMatchers.is(23)))
                .andExpect(jsonPath("$.hourly_forecast[0].temperature", CoreMatchers.is(23)))
                .andExpect(jsonPath("$.hourly_forecast[1].precipitation", CoreMatchers.is(95)))
                .andExpect(jsonPath("$.hourly_forecast[1].status", CoreMatchers.is("Raining")))
                .andExpect(jsonPath("$.realtime_weather.status", CoreMatchers.is("Raining")))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full/DELHI_IN")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200Ok() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

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

        mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.realtime_weather.temperature", CoreMatchers.is(25)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", CoreMatchers.is(13)))
                .andExpect(jsonPath("$.daily_forecast[0].precipitation", CoreMatchers.is(23)))
                .andExpect(jsonPath("$._links.self.href", CoreMatchers.is("http://localhost/v1/full/NYC_USA")))
                .andDo(print());
    }

}
