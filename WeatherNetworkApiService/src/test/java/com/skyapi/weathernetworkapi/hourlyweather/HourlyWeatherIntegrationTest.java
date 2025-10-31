package com.skyapi.weathernetworkapi.hourlyweather;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles({"testing"/*,"permitAllRequestForTest"*/})
@Sql(scripts = "classpath:drop.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/data-test.sql"},config = @SqlConfig(separator = ScriptUtils.DEFAULT_STATEMENT_SEPARATOR))
@AutoConfigureMockMvc
public class HourlyWeatherIntegrationTest {

    private static final String END_POINT_PATH = "/v1/hourly";

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
    public void testGeHourlyWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(get(requestUrl).header("X-Current-Hour",8))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testHourlyWeatherByApiAddressShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","103.48.198.141").header("X-Current-Hour",8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                /*тут запрос на список тому це масив обєктів з полями і [0] - це перший обєкти в масиві і його поля code і city_name*/
                .andExpect(jsonPath("$.location",is("DELHI_IN => Delhi, Delhi, India")))
                .andExpect(jsonPath("$.hourly_forecast[1].temperature",is(25)))
                .andExpect(jsonPath("$.hourly_forecast[1].status",is("Sunny")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testHourlyWeatherByApiAddressShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl).header("X-Forwarded-For","107.48.198.141").header("X-Current-Hour",8))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetHourlyWeatherByLocationCodeShouldReturn200isOk() throws Exception {
        String locationCode = "DELHI_IN";
        String requestUrl = END_POINT_PATH + "/" + "DELHI_IN";
        mockMvc.perform(get(requestUrl).header("X-Current-Hour",8))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                /*тут запрос на список тому це масив обєктів з полями і [0] - це перший обєкти в масиві і його поля code і city_name*/
                .andExpect(jsonPath("$.location",is("DELHI_IN => Delhi, Delhi, India")))
                .andExpect(jsonPath("$.hourly_forecast[0].temperature",is(21)))
                .andExpect(jsonPath("$.hourly_forecast[0].precipitation",is(95)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly" + "/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime"+ "/"  + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily"+ "/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full"+ "/"  + locationCode)))
                .andDo(print());
    }

    @Test
    public void testGeHourlyWeatherByLocationCodeShouldReturn400BadRequest() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGeHourlyWeatherByLocationIpAddressShouldReturn400BadRequest() throws Exception {
        String requestUrl = END_POINT_PATH;
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus400BadRequestBecauseNoData() throws Exception {
        String locationCode = "AVC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        List<HourlyWeatherDTO> listDTO = new ArrayList<>();
        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType("application/json").content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Hourly forecast data cannot be empty")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO forecast1 = new HourlyWeatherDTO().hourOfDay(10).temperature(20).precipitation(5).status("Sunny");
        HourlyWeatherDTO forecast2 = new HourlyWeatherDTO().hourOfDay(11).temperature(27).precipitation(80).status("Rain");
        HourlyWeatherDTO forecast3 = new HourlyWeatherDTO().hourOfDay(12).temperature(30).precipitation(60).status("Cloudy");

        List<HourlyWeatherDTO> weatherDTOS = new ArrayList<>();
        weatherDTOS.addAll(List.of(forecast1,forecast2,forecast3));

        String bodyContent = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly" + "/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime"+ "/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily"+ "/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full"+ "/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "N_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO forecast1 = new HourlyWeatherDTO().hourOfDay(10).temperature(20).precipitation(5).status("Sunny");
        HourlyWeatherDTO forecast2 = new HourlyWeatherDTO().hourOfDay(11).temperature(27).precipitation(80).status("Rain");
        HourlyWeatherDTO forecast3 = new HourlyWeatherDTO().hourOfDay(12).temperature(30).precipitation(60).status("Cloudy");

        List<HourlyWeatherDTO> weatherDTOS = new ArrayList<>();
        weatherDTOS.addAll(List.of(forecast1,forecast2,forecast3));

        String bodyContent = objectMapper.writeValueAsString(weatherDTOS);

        mockMvc.perform(put(requestURI).contentType("application/json").content(bodyContent))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
