package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherApiController;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherApiController;
import com.skyapi.weathernetworkapi.globalerror.BadRequestException;
import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherApiController;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherApiController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/locations")
@Validated//для роботи @Min, @Max в методах
public class LocationApiController {

    private LocationService locationService;
    private ModelMapper modelMapper;

    private Map<String, String> propertyMap = Map.of(
            "code", "code",
            "city_name", "cityName",
            "region_name", "regionName",
            "country_code", "countryCode",
            "country_name", "countryName",
            "enabled", "enabled"
    );

    @Autowired
    public LocationApiController(LocationService locationService, ModelMapper modelMapper) {
        this.locationService = locationService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<LocationDTO> addLocation(@Valid @RequestBody LocationDTO dto) throws GeolocatioException {
        Location addedLocation = locationService.addLocation(dtoToEntity(dto));
        URI uri = URI.create("/v1/locations/" + addedLocation.getCode());
        return ResponseEntity.created(uri).body(addLinksToItem(entityToDTO(addedLocation)));
    }

    private LocationDTO addLinksToItem(LocationDTO dto) throws GeolocatioException {

        dto.add(linkTo(
                methodOn(LocationApiController.class).getLocationByCode(dto.getCode()))
                .withSelfRel());

        dto.add(linkTo(
                methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(dto.getCode()))
                .withRel("realtime_weather"));

        dto.add(linkTo(
                methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(dto.getCode(), null))
                .withRel("hourly_forecast"));

        dto.add(linkTo(
                methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(dto.getCode()))
                .withRel("daily_forecast"));

        dto.add(linkTo(
                methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(dto.getCode()))
                .withRel("full_forecast"));

        return dto;
    }

    @GetMapping
    public ResponseEntity<?> listLocations(
            @RequestParam(value = "pageNumber", required = false, defaultValue = "1") @Min(value = 1) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "5") @Min(value = 2) @Max(value = 20) Integer pageSize,
            @RequestParam(value = "sortField", required = false, defaultValue = "code") String sortOption,
            @RequestParam(value = "enabled", required = false, defaultValue = "") String enabled,
            @RequestParam(value = "region_name", required = false, defaultValue = "") String regionName,
            @RequestParam(value = "country_code", required = false, defaultValue = "") String countryCode) throws BadRequestException, GeolocatioException {

        sortOption = validateSortOption(sortOption);

        Map<String, Object> filterFields = getFilterFields(enabled, regionName, countryCode);

        Page<Location> page = locationService.listByPage(pageNumber - 1, pageSize, sortOption, filterFields);

        List<Location> locations = page.getContent();

        if (locations.isEmpty()) {

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        } else {

            HttpHeaders headers = new HttpHeaders();
            headers.setExpires(Instant.now().plus(7, ChronoUnit.DAYS));

            return new ResponseEntity<>(addPageMetaDataAndLinksToCollections(listEntityToListDTO(locations), page, sortOption, enabled, regionName, countryCode),headers, HttpStatus.OK);
        }
    }

    private static Map<String, Object> getFilterFields(String enabled, String regionName, String countryCode) {
        Map<String, Object> filterFields = new HashMap<>();
        if (!"".equals(enabled)) {
            filterFields.put("enabled", Boolean.parseBoolean(enabled));
        }
        if (!"".equals(regionName)) {
            filterFields.put("regionName", regionName);
        }
        if (!"".equals(countryCode)) {
            filterFields.put("countryCode", countryCode);
        }
        return filterFields;
    }

    private String validateSortOption(String sortOption) throws BadRequestException {
        String translatedSortOption = sortOption;

        String[] sortFields = sortOption.split(",");

        if(sortFields.length > 1) {
            for(int i  = 0; i < sortFields.length; i++) {
                String actualFieldName = sortFields[i].replace("-", "");

                if(!propertyMap.containsKey(actualFieldName)) {
                    throw new BadRequestException("Invalid sort field: " + actualFieldName);
                }

                translatedSortOption = translatedSortOption.replace(actualFieldName, propertyMap.get(actualFieldName));
            }
        } else{
            String actualFieldName  = sortOption.replace("-", "");
            if (!propertyMap.containsKey(actualFieldName)) {
                throw new BadRequestException("Invalid sort field: " + sortOption);
            }

            translatedSortOption = translatedSortOption.replace(actualFieldName, propertyMap.get(actualFieldName));
        }
        return translatedSortOption;
    }

    private CollectionModel<LocationDTO> addPageMetaDataAndLinksToCollections(List<LocationDTO> listDTO, Page<Location> pageInfo, String sortField, String enabled, String regionName, String countryCode) throws BadRequestException, GeolocatioException {

        String actualEnabled = "".equals(enabled) ? null : enabled;
        String actualRegionName = "".equals(regionName) ? null : regionName;
        String actualCountryCode = "".equals(countryCode) ? null : countryCode;

        for (LocationDTO locationDTO : listDTO) {
            locationDTO.add(linkTo(methodOn(LocationApiController.class).getLocationByCode(locationDTO.getCode())).withSelfRel());
        }
        int pageSize = pageInfo.getSize();
        int pageNumber = pageInfo.getNumber() + 1;
        long totalElements = pageInfo.getTotalElements();
        int totalPages = pageInfo.getTotalPages();

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, pageNumber, totalElements);
        CollectionModel<LocationDTO> collectionModel = PagedModel.of(listDTO, pageMetadata);

        collectionModel.add(linkTo(methodOn(LocationApiController.class)
                .listLocations(pageNumber, pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode)).withSelfRel());

        if (pageNumber > 1) {
            collectionModel.add(linkTo(methodOn(LocationApiController.class).listLocations(1, pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.FIRST));
            collectionModel.add(linkTo(methodOn(LocationApiController.class).listLocations(pageNumber - 1, pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.PREV));
        }
        if (pageNumber < totalPages) {
            collectionModel.add(linkTo(methodOn(LocationApiController.class).listLocations(pageNumber + 1, pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.NEXT));
            collectionModel.add(linkTo(methodOn(LocationApiController.class).listLocations(totalPages, pageSize, sortField, actualEnabled, actualRegionName, actualCountryCode)).withRel(IanaLinkRelations.LAST));
        }
        return collectionModel;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getLocationByCode(@PathVariable("code") String code) throws GeolocatioException {
        Location location = locationService.getLocationByCode(code);

        String eTag = "\"" + Objects.hash(location.getCode(), location.getCityName(), location.getRegionName(),location.getCountryCode(), location.getCountryName()) + "\"";

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ifNoneMatch = servletRequest.getHeader("If-None-Match");

        if(eTag.equals(ifNoneMatch)) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic()).eTag(eTag).body(addLinksByLocationCode(entityToDTO(location)));
    }

    private LocationDTO addLinksByLocationCode (LocationDTO locationDTO) throws GeolocatioException {

        locationDTO.add(linkTo(methodOn(LocationApiController.class).getLocationByCode(locationDTO.getCode())).withSelfRel());

        locationDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealTimeWeatherByLocationCode(locationDTO.getCode())).withRel("realtime"));

        locationDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).getHourlyWeatherByLocationCode(locationDTO.getCode(),null)).withRel("hourly_forecast"));

        locationDTO.add(linkTo(methodOn(DailyWeatherApiController.class).getDailyWeatherByLocationCode(locationDTO.getCode())).withRel("daily_forecast"));

        locationDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationDTO.getCode())).withRel("full_forecast"));

        return locationDTO;
    }

    private List<LocationDTO> listEntityToListDTO(List<Location> listEntity) {
        return listEntity.stream().map(entity -> entityToDTO(entity)).collect(Collectors.toList());
    }

    private LocationDTO entityToDTO(Location entity) {
        return modelMapper.map(entity, LocationDTO.class);
    }

    private Location dtoToEntity(LocationDTO dto) {
        return modelMapper.map(dto, Location.class);
    }

    @PutMapping
    public ResponseEntity<?> updateLocation(@Valid @RequestBody LocationDTO locationDTO) throws GeolocatioException {
        Location updatedLocation = locationService.updateLocation(dtoToEntity(locationDTO));
        return new ResponseEntity<>(addLinksToItem(entityToDTO(updatedLocation)), HttpStatus.OK);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Location> trashedLocationByCode(@PathVariable("code") String code) {
        locationService.trashedLocation(code);
        return ResponseEntity.noContent().build();
    }
}
