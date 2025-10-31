package com.skyapi.weathernetworkapi.realtimeweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles({"testing"})
@Sql(scripts = "classpath:drop.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/data-test.sql"},config = @SqlConfig(separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR))
@AutoConfigureMockMvc
public class RealtimeWeatherIntegrationTest {

    private static final String END_POINT_PATH = "/v1/realtime";

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
    public void testGetByCodeShouldReturn405NotAllowed() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(post(requestUrl))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }

    @Test
    public void testGeRealtimeWeathertByLocationCodeShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testRealtimeWeatherByApiAddressShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","108.30.178.78"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.location",is("NYC_USA => New York City, New York, United States of America")))
                .andExpect(jsonPath("$.temperature",is(-20)))
                .andExpect(jsonPath("$.wind_speed",is(18)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testRealtimeWeatherByApiAddressShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","197.30.178.78"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetRealtimeWeatherByLocationCodeShouldReturn200isOk() throws Exception {
        String locationCode = "NYC_USA";
        String requestUrl = END_POINT_PATH + "/" + locationCode;
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                /*тут запрос на список тому це масив обєктів з полями і [0] - це перший обєкти в масиві і його поля code і city_name*/
                .andExpect(jsonPath("$.location",is("NYC_USA => New York City, New York, United States of America")))
                .andExpect(jsonPath("$.temperature",is(-20)))
                .andExpect(jsonPath("$.wind_speed",is(18)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testGetRealtimeWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH + "/NYAI";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus400BadRequest() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(250);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);

        String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);

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

    @Test
    public void testUpdateShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "N_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(25);
        realtimeWeather.setHumidity(52);
        realtimeWeather.setPrecipitation(15);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(10);

        String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
