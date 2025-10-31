package com.skyapi.weathernetworkapi.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationApiController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfigurationWebMvcTests.class)
@ActiveProfiles({/*"testing",*/"permitAllRequestForTest"})
@AutoConfigureMockMvc
public class LocationControllerTest {

    private static final String END_POINT_PATH = "/v1/locations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private LocationRepository locationRepository;


    @Test
    public void testAddShouldReturn201Created() throws Exception {
        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        when(locationService.addLocation(location)).thenReturn(location);

        String bodyContent = objectMapper.writeValueAsString(location);
        mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.code",is("NYC_USA")))
                .andExpect(jsonPath("$.city_name",is("New York City")))
                .andExpect(header().string("Location",is("/v1/locations/NYC_USA")))
                .andDo(print());
    }

    @Test
    public void testValidateRequestBodyLocationCodeNotNull() throws Exception {
        Location location = new Location();
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        when(locationService.addLocation(location)).thenReturn(location);

        String bodyContent = objectMapper.writeValueAsString(location);
        mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.errors[0]",is("Location code cannot be null")))
                .andDo(print());
    }

    @Test
    public void testValidateRequestBodyLocationCodeLength() throws Exception {
        Location location = new Location();
        location.setCode("");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        when(locationService.addLocation(location)).thenReturn(location);

        String bodyContent = objectMapper.writeValueAsString(location);
        mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.errors[0]",is("Location code must have 3-12 characters")))
                .andDo(print());
    }

    @Test
    public void testValidateRequestBodyAllFieldsInvalid() throws Exception {
        Location location = new Location();
        location.setRegionName("N");

        when(locationService.addLocation(location)).thenReturn(location);

        String bodyContent = objectMapper.writeValueAsString(location);

       MvcResult mvcResult = mockMvc.perform(post(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andDo(print())
               .andReturn();
       String response = mvcResult.getResponse().getContentAsString();
       assertThat(response).contains("Region name must have 3-128 characters");
        assertThat(response).contains("Location code cannot be null");
        assertThat(response).contains("City name cannot be null");
        assertThat(response).contains("Country name cannot be null");
        assertThat(response).contains("Country code cannot be null");
    }

    @Test
    public void testGetLocationReturn404NotFound() throws Exception {
        String requestCode = "/asdc";
        when(locationService.getLocationByCode(anyString())).thenThrow(LocationNotFoundException.class);
        mockMvc.perform(get(END_POINT_PATH + requestCode))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @Disabled
    public void testListShouldReturn200OK() throws Exception {
        Location location1 = new Location();
        location1.setCode("NYC_USA");
        location1.setCityName("New York City");
        location1.setCountryCode("US");
        location1.setRegionName("New York");
        location1.setCountryName("United States of America");
        location1.setEnabled(true);

        Location location2 = new Location();
        location2.setCode("LACA_USA");
        location2.setCityName("Los Angeles");
        location2.setCountryCode("US");
        location2.setRegionName("California");
        location2.setCountryName("United States of America");
        location2.setEnabled(true);

        Location location3 = new Location();
        location3.setCode("DELHI_IN");
        location3.setCityName("New Delhi");
        location3.setCountryCode("IN");
        location3.setRegionName("Delhi");
        location3.setCountryName("India");
        location3.setEnabled(true);

        when(locationService.getAllLocations()).thenReturn(List.of(location1, location2, location3));
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                /*тут запрос на список тому це масив обєктів з полями і [0] - це перший обєкти в масиві і його поля code і city_name*/
                .andExpect(jsonPath("$[0].code",is("NYC_USA")))
                .andExpect(jsonPath("$[0].city_name",is("New York City")))
                .andExpect(jsonPath("$[1].code",is("LACA_USA")))
                .andExpect(jsonPath("$[1].city_name",is("Los Angeles")))
                .andDo(print());
    }

    @Test
    public void testListByPageShouldReturn200OK() throws Exception {
        Location location1 = new Location();
        location1.setCode("NYC_USA");
        location1.setCityName("New York City");
        location1.setCountryCode("US");
        location1.setRegionName("New York");
        location1.setCountryName("United States of America");
        location1.setEnabled(true);

        Location location2 = new Location();
        location2.setCode("LACA_USA");
        location2.setCityName("Los Angeles");
        location2.setCountryCode("US");
        location2.setRegionName("California");
        location2.setCountryName("United States of America");
        location2.setEnabled(true);

        Location location3 = new Location();
        location3.setCode("DELHI_IN");
        location3.setCityName("New Delhi");
        location3.setCountryCode("IN");
        location3.setRegionName("Delhi");
        location3.setCountryName("India");
        location3.setEnabled(true);

        List<Location> listLocations = List.of(location1, location2, location3);
        int pageNumber = 1;
        int pageSize = 5;
        String sortField = "code";
        int totalElements = listLocations.size();

        Sort sort = Sort.by(sortField);
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
        Page<Location> page = new PageImpl<>(listLocations,pageable,totalElements);

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(page);

        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().exists("Expires"))
                .andExpect(jsonPath("$._embedded.locations[0].code",is("NYC_USA")))
                .andExpect(jsonPath("$._embedded.locations[0].city_name",is("New York City")))
                .andExpect(jsonPath("$._embedded.locations[1].code",is("LACA_USA")))
                .andExpect(jsonPath("$._embedded.locations[1].city_name",is("Los Angeles")))
                .andExpect(jsonPath("$.page.size",is(pageSize)))
                .andExpect(jsonPath("$.page.number",is(pageNumber)))
                .andExpect(jsonPath("$.page.total_elements",is(3)))
                .andDo(print());
    }

    @Test
    public void testGetLocationByCodeLReturn200() throws Exception {
        String locationCode = "NYC_USA";
        String url = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        when(locationService.getLocationByCode(locationCode)).thenReturn(location);

        mockMvc.perform(get(url).contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(header().string("Cache-Control", containsString("max-age=604800")))
                .andExpect(header().exists("ETag"))
                .andExpect(jsonPath("$.code",is(locationCode)))
                .andExpect(jsonPath("$.city_name",is("New York City")))
                .andExpect(jsonPath("$._links.self.href",is("http://localhost" + END_POINT_PATH + "/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testPaginationLinksOnlyOnePage() throws Exception {
        Location location1 = new Location("NYC_USA","New York City","US","New York","United States of America",true);
        Location location2 = new Location("LACA_USA","Los Angeles","US","California","United States of America",true);

        List<Location> listLocations = List.of(location1, location2);
        int pageNumber = 1;
        int pageSize = 5;
        String sortField = "code";
        int totalElements = listLocations.size();

        Sort sort = Sort.by(sortField);
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
        Page<Location> page = new PageImpl<>(listLocations,pageable,totalElements);

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(page);

        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String host = "http://localhost";

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href",containsString(host+requestURI)))
                .andExpect(jsonPath("$._links.first").doesNotExist())
                .andExpect(jsonPath("$._links.next").doesNotExist())
                .andExpect(jsonPath("$._links.prev").doesNotExist())
                .andExpect(jsonPath("$._links.last").doesNotExist())
                .andDo(print());
    }

    @Test
    public void testPaginationLinksInFirstPage() throws Exception {

        int pageNumber = 1;
        int pageSize = 5;
        String sortField = "code";
        int totalElements = 18;
        int totalPages = totalElements/pageSize+1;

        List<Location> listLocations = new ArrayList<>(pageSize);
        for (int i = 1; i <= pageSize; i++) {
            listLocations.add(new Location("Code_" +i, "city " + i,"region_name","US","country name"));
        }

        Sort sort = Sort.by(sortField);
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
        Page<Location> page = new PageImpl<>(listLocations,pageable,totalElements);

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(page);

        String host = "http://localhost";
        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String nextPageURI = END_POINT_PATH + "?pageNumber=" + (pageNumber+1) + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String lastPageURI = END_POINT_PATH + "?pageNumber=" + totalPages + "&pageSize=" + pageSize + "&sortField=" + sortField;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.first").doesNotExist())
                .andExpect(jsonPath("$._links.next.href",containsString(host+nextPageURI)))
                .andExpect(jsonPath("$._links.prev").doesNotExist())
                .andExpect(jsonPath("$._links.last.href",containsString(host+lastPageURI)))
                .andDo(print());
    }

    @Test
    public void testPaginationLinksInMiddlePage() throws Exception {

        int pageNumber = 3;
        int pageSize = 5;
        String sortField = "code";
        int totalElements = 18;
        int totalPages = totalElements/pageSize+1;

        List<Location> listLocations = new ArrayList<>(pageSize);
        for (int i = 1; i <= pageSize; i++) {
            listLocations.add(new Location("Code_" +i, "city " + i,"region_name","US","country name"));
        }

        Sort sort = Sort.by(sortField);
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize, sort);
        Page<Location> page = new PageImpl<>(listLocations,pageable,totalElements);

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(page);

        String host = "http://localhost";
        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String firstPageURI = END_POINT_PATH + "?pageNumber=1&pageSize=" + pageSize + "&sortField=" + sortField;
        String nextPageURI = END_POINT_PATH + "?pageNumber=" + (pageNumber+1) + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String previousPageURI = END_POINT_PATH + "?pageNumber=" + (pageNumber-1) + "&pageSize=" + pageSize + "&sortField=" + sortField;
        String lastPageURI = END_POINT_PATH + "?pageNumber=" + totalPages + "&pageSize=" + pageSize + "&sortField=" + sortField;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$._links.first.href",containsString(host+firstPageURI)))
                .andExpect(jsonPath("$._links.next.href",containsString(host+nextPageURI)))
                .andExpect(jsonPath("$._links.prev.href",containsString(host+previousPageURI)))
                .andExpect(jsonPath("$._links.last.href",containsString(host+lastPageURI)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setCountryCode("US");
        location.setRegionName("New York");
        location.setCountryName("United States of America");
        location.setEnabled(true);

        when(locationService.updateLocation(location)).thenReturn(location);

        /*Method that can be used to serialize any Java value as a String*/
        String bodyContent = objectMapper.writeValueAsString(location);
        mockMvc.perform(put(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.code",is("NYC_USA")))
                .andExpect(jsonPath("$.city_name",is("New York City")))
                .andDo(print());
    }

    @Test
    public void updateShouldReturn404NotFound() throws Exception {
        Location location = new Location();
        location.setCode("ADVCGB");
        location.setCityName("New Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        String bodyContent = objectMapper.writeValueAsString(location);
        when(locationService.updateLocation(location)).thenThrow(new LocationNotFoundException("No location found"));

        mockMvc.perform(put(END_POINT_PATH).contentType("application/json").content(bodyContent))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void updateShouldReturn400BadRequest() throws Exception {
        Location location = new Location();
        location.setCityName("New Delhi");
        location.setCountryCode("IN");
        location.setRegionName("Delhi");
        location.setCountryName("India");
        location.setEnabled(true);

        String bodyContent = objectMapper.writeValueAsString(location);

        mockMvc.perform(put(END_POINT_PATH).contentType("application/hal+json").content(bodyContent))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void trashedLocationShouldReturn400BadRequest() throws Exception {
        String code = "ADVCGB";
        String requestURI = END_POINT_PATH + "/" + code;
        doThrow(LocationNotFoundException.class).when(locationService).trashedLocation(code);

        mockMvc.perform(delete(requestURI))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void trashedLocationShouldReturn204NoContent() throws Exception {
        String code = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + code;
        doNothing().when(locationService).trashedLocation(code);

        mockMvc.perform(delete(requestURI))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @Disabled
    public void testListShouldReturn204NoContent() throws Exception {
        when(locationService.getAllLocations()).thenReturn(Collections.EMPTY_LIST);
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testListByPageShouldReturn204NoContent() throws Exception {
        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(Page.empty());
        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testListByPageShouldReturn400BadRequestInvalidPageNumber() throws Exception {
        int pageNumber = 0;
        int pageSize = 1;
        String sortField = "code";

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(Page.empty());
        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;
        mockMvc.perform(get(requestURI))
                .andExpect(status().isBadRequest())
                .andExpect( jsonPath("$[?(@.errors =~ /.*must be greater than or equal to 5*./)].errors").hasJsonPath())
                .andExpect( jsonPath("$[?(@.errors =~ /.*must be greater than or equal to 1*./)].errors").hasJsonPath())
                .andDo(print());
    }

    @Test
    public void testListByPageShouldReturn400BadRequestInvalidSortField() throws Exception {
        int pageNumber = 1;
        int pageSize = 5;
        String sortField = "code_ans";

        when(locationService.listByPage(anyInt(),anyInt(),anyString(),anyMap())).thenReturn(Page.empty());
        String requestURI = END_POINT_PATH + "?pageNumber=" + pageNumber + "&pageSize=" + pageSize + "&sortField=" + sortField;
        mockMvc.perform(get(requestURI))
                .andExpect(status().isBadRequest())
                .andExpect( jsonPath("$[?(@.errors =~ /.*Invalid sort field: code_ans*./)].errors").hasJsonPath())
                .andDo(print());
    }
}
