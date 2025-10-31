package com.skyapi.weathernetworkapi.dailyweather;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@Sql(scripts = "classpath:drop.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/data-test.sql"},config = @SqlConfig(separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR))
@AutoConfigureMockMvc
public class DailyWeatherIntegrationTest {

    private static final String END_POINT_PATH = "/v1/daily";

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
    public void testDailyWeatherByApiAddressShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","197.30.178.78"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testDailyWeatherByApiAddressShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","103.48.198.141"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.location",is("DELHI_IN => Delhi, Delhi, India")))
                .andExpect(jsonPath("$.daily_forecast[1].max_temperature",is(28)))
                .andExpect(jsonPath("$.daily_forecast[1].status",is("Cloudy")))
                .andExpect(jsonPath("$.daily_forecast[2].min_temperature",is(19)))
                .andExpect(jsonPath("$.daily_forecast[2].precipitation",is(95)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGeDailyWeatherByLocationIpAddressShouldReturn204NoContent() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","195.235.92.26"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetDailyWeatherByLocationCodeShouldReturn200isOk() throws Exception {
        String locationCode = "DELHI_IN";
        String requestUrl = END_POINT_PATH + "/" + locationCode;
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.daily_forecast[1].max_temperature",is(28)))
                .andExpect(jsonPath("$.daily_forecast[1].status",is("Cloudy")))
                .andExpect(jsonPath("$.daily_forecast[2].min_temperature",is(19)))
                .andExpect(jsonPath("$.daily_forecast[2].precipitation",is(95)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus400BadRequestBecauseNoData() throws Exception {
        String locationCode = "AVC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        List<DailyWeatherDTO> listDTO = new ArrayList<>();
        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType("application/json").content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Daily forecast data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        DailyWeatherDTO forecast1 = new DailyWeatherDTO().dayOfMonth(1).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");
        DailyWeatherDTO forecast2 = new DailyWeatherDTO().dayOfMonth(2).month(7).min_temperature(25).max_temperature(27).precipitation(60).status("Cloudy");
        DailyWeatherDTO forecast3 = new DailyWeatherDTO().dayOfMonth(3).month(7).min_temperature(20).max_temperature(23).precipitation(90).status("Rain");

        List<DailyWeatherDTO> weatherDTOS = new ArrayList<>();
        weatherDTOS.addAll(List.of(forecast1,forecast2,forecast3));

        String bodyContent = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "N_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        DailyWeatherDTO forecast1 = new DailyWeatherDTO().dayOfMonth(1).month(7).min_temperature(28).max_temperature(31).precipitation(25).status("Sunny");

        List<DailyWeatherDTO> weatherDTOS = List.of(forecast1);
        String bodyContent = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isNotFound())
                .andExpect( jsonPath("$[?(@.errors =~ /.*No location found with the given location code: N_USA*./)].errors").hasJsonPath())
                .andDo(print());
    }
}
