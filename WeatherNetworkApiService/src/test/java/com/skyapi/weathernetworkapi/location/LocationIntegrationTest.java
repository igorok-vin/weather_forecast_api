package com.skyapi.weathernetworkapi.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.common.Location;
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
public class LocationIntegrationTest {

    private static final String END_POINT_PATH = "/v1/locations";

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
    public void testAddShouldReturn400BadRequest() throws Exception {
        Location location = new Location();

        String bodyContent = objectMapper.writeValueAsString(location);
        mockMvc.perform(post(END_POINT_PATH).contentType("application/hal+json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn405NotAllowed() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(post(requestUrl))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn404NotFound() throws Exception {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetByCodeShouldReturn200isOk() throws Exception {
        String requestUrl = END_POINT_PATH + "/NYC_USA";
        mockMvc.perform(get(requestUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.code",is("NYC_USA")))
                .andExpect(jsonPath("$.city_name",is("New York City")))
                .andDo(print());
    }

    @Test
    public void testTrashedByCodeShouldReturn404NotFound() throws Exception  {
        String requestUrl = END_POINT_PATH + "/AJFLD";
        mockMvc.perform(delete(requestUrl))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testTrashedByCodeShouldReturn204isOk() throws Exception  {
        String requestUrl = END_POINT_PATH + "/NYC_USA";
        mockMvc.perform(delete(requestUrl))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}
