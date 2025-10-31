package com.skyapi.weathernetworkapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skyapi.weathernetworkapi.common.Location;
import com.skyapi.weathernetworkapi.common.RealtimeWeather;
import com.skyapi.weathernetworkapi.common.dailyweather.DailyWeather;
import com.skyapi.weathernetworkapi.common.hourlyweather.HourlyWeather;
import com.skyapi.weathernetworkapi.dailyweather.DailyWeatherDTO;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherDTO;
import com.skyapi.weathernetworkapi.fullweather.FullWeatherModelAssembler;
import com.skyapi.weathernetworkapi.hourlyweather.HourlyWeatherDTO;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;
import com.skyapi.weathernetworkapi.security.RsaKeyProperties;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
@EnableCaching
public class WeatherNetworkApiServiceApplication {

    @Bean
    public ModelMapper getMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        configureMappingFromHourlyWeatherToHourlyWeatherDTO(modelMapper);

        configureMappingFromDailyWeatherToDailyWeatherDTO(modelMapper);

        configureMappingFromDailyWeatherDTOToDailyWeather(modelMapper);

        configureMappingFromLocationToFullWeatherDTO(modelMapper);

        configureMappingForRealtimeWeather(modelMapper);

        return modelMapper;
    }

    private static void configureMappingFromLocationToFullWeatherDTO(ModelMapper modelMapper) {
         modelMapper.typeMap(Location.class, FullWeatherDTO.class).addMapping(source -> source.toString(),FullWeatherDTO::setLocation);
    }

    private static void configureMappingFromDailyWeatherDTOToDailyWeather(ModelMapper modelMapper) {
        TypeMap<DailyWeatherDTO, DailyWeather> dailyWeatherDTOToHourlyWeather1 = modelMapper.typeMap(DailyWeatherDTO.class, DailyWeather.class);
        dailyWeatherDTOToHourlyWeather1.addMapping(source -> source.getDayOfMonth(),DailyWeather::dayOfMonth);

        TypeMap<DailyWeatherDTO, DailyWeather> dailyWeatherDTOToHourlyWeather2 = modelMapper.typeMap(DailyWeatherDTO.class, DailyWeather.class);
        dailyWeatherDTOToHourlyWeather2.addMapping(source -> source.getMonth(),DailyWeather::month);
    }

    private static void configureMappingFromHourlyWeatherToHourlyWeatherDTO(ModelMapper modelMapper) {
        var typeMap1= modelMapper.typeMap(HourlyWeather.class, HourlyWeatherDTO.class);
        typeMap1.addMapping(src -> src.getId().getHourOfDay(), HourlyWeatherDTO::setHourOfDay);

        var typeMap2= modelMapper.typeMap(HourlyWeatherDTO.class, HourlyWeather.class);
        typeMap2.addMapping(src -> src.getHourOfDay(), (destination, value) -> destination.getId().setHourOfDay(value != null? (int) value : 0));
    }

    private static void configureMappingFromDailyWeatherToDailyWeatherDTO(ModelMapper modelMapper) {
        TypeMap<DailyWeather, DailyWeatherDTO> dailyWeatherToDailyWeatherDTO1 = modelMapper.typeMap(DailyWeather.class, DailyWeatherDTO.class);
        dailyWeatherToDailyWeatherDTO1.addMapping(source -> source.getId().getDayOfMonth(), DailyWeatherDTO::setDayOfMonth);

        TypeMap<DailyWeather, DailyWeatherDTO> dailyWeatherToDailyWeatherDTO2 = modelMapper.typeMap(DailyWeather.class, DailyWeatherDTO.class);
        dailyWeatherToDailyWeatherDTO2.addMapping(source -> source.getId().getMonth(), DailyWeatherDTO::setMonth);
    }

    private void configureMappingForRealtimeWeather(ModelMapper mapper) {
        mapper.typeMap(RealtimeWeatherDTO.class, RealtimeWeather.class)
                .addMappings(m -> m.skip(RealtimeWeather::setLocation));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return objectMapper;
    }

    @Bean
    public FullWeatherModelAssembler fullWeatherModelAssembler() {
        return new FullWeatherModelAssembler();
    }

    public static void main(String[] args) {
        SpringApplication.run(WeatherNetworkApiServiceApplication.class, args);
    }

}
