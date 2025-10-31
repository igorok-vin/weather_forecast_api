package com.skyapi.weathernetworkapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherDTO;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherDTO;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest {

    private static final String GET_ACCESS_TOKEN_ENDPOINT = "/oauth2/token";
    private static final String LOCATION_API_ENDPOINT = "/v1/locations";
    private static final String REALTIME_ENDPOINT_PATTERN = "/v1/realtime";
    private static final String HOURLY_ENDPOINT_PATTERN = "/v1/hourly";
    private static final String DAILY_ENDPOINT_PATTERN = "/v1/daily";
    private static final String FULL_ENDPOINT_PATTERN = "/v1/full";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getAccessTokenFailed() throws Exception {
        mvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
                .param("client_id", "abc")
                .param("client_secret", "fsdf")
                .param("grant_type", "client_credentials"))
            .andExpect(status().isUnauthorized())
            .andDo(print())
            .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    public void getAccessTokenSuccess() throws Exception {
        mvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
                        .param("client_id", "hnLqPPQqGogt7GtgIJbh")
                        .param("client_secret", "YLnMa6solui0BAjG2pWkzpU4rtkDEvtWVAqiTddB")
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andExpect(jsonPath("$.token_type",is("Bearer")));
    }

    @Test
    public void testGetBaseURI() throws Exception {
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testListLocationWithScopeReader() throws Exception {
        mockMvc.perform(get(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testListLocationWithUnknownScope() throws Exception {
        mockMvc.perform(get(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt->jwt.claim("scope", "USER"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAddListLocationWithScopeReader() throws Exception {
        Location location = new Location("X","Test","Test","Test","Test",true);
        String requestBody = mapper.writeValueAsString(location);

        mockMvc.perform(post(LOCATION_API_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestBody).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAddListLocationWithScopeSYSTEM() throws Exception {
        Location location = new Location("X","Test","Test","Test","Test",true);
        String requestBody = mapper.writeValueAsString(location);

        mockMvc.perform(post(LOCATION_API_ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(requestBody).with(jwt().jwt(jwt->jwt.claim("scope", "SYSTEM"))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetRealtimeWeatherWithScopeReaderNotFound() throws Exception {
        String requestURL = REALTIME_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(get(requestURL).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))))
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]", is("No realtime weather data found with the given location code: code")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateRealtimeWeatherWithScopeReader() throws Exception {
        RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
        String  requestBody = mapper.writeValueAsString(realtimeWeatherDTO);

        String requestURL = REALTIME_ENDPOINT_PATTERN + "/code";

        MvcResult mvcResult = mockMvc.perform(put(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "READER"))).content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<String> headers = response.getHeaders("WWW-Authenticate");

        assertThat(headers.contains("The request requires higher privileges than provided by the access token."));
    }

    @Test
    public void testUpdateRealtimeWeatherWithScopeSystem() throws Exception {
        RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
        String  requestBody = mapper.writeValueAsString(realtimeWeatherDTO);

        String requestURL = REALTIME_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(put(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM"))).content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]", is("Status must not be empty")));
    }

    @Test
    public void testGetHourlyWeatherWithScopeReaderNotFound() throws Exception {
        String requestURL = HOURLY_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(get(requestURL).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))).header("X-Current-Hour", "10"))
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]", is("No location found with the given location code: code")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateHourlyWeatherWithScopeReader() throws Exception {
        String requestURL = HOURLY_ENDPOINT_PATTERN + "/code";

        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
        String  requestBody = mapper.writeValueAsString(hourlyWeatherDTO);

        MvcResult mvcResult = mockMvc.perform(put(requestURL).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))).content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<String> headers = response.getHeaders("WWW-Authenticate");

        assertThat(headers.contains("The request requires higher privileges than provided by the access token."));
    }

    @Test
    public void testUpdateHourlyWeatherWithScopeSystem() throws Exception {
        HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
        String  requestBody = mapper.writeValueAsString(hourlyWeatherDTO);

        String requestURL = HOURLY_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(put(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM"))).content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Failed to read request")));
    }

    @Test
    public void testGetDailyWeatherWithScopeReaderNotFound() throws Exception {
        String requestURL = DAILY_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(get(requestURL).with(jwt().jwt(jwt->jwt.claim("scope", "READER"))))
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]", is("No location found with the given location code: code")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateDailyWeatherWithScopeSystem() throws Exception {
        DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
        String  requestBody = mapper.writeValueAsString(dailyWeatherDTO);

        String requestURL = DAILY_ENDPOINT_PATTERN + "/code";

        mockMvc.perform(put(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM"))).content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Failed to read request")));
    }

}
