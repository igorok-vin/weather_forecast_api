package com.skyapi.weathernetworkapi.fullweather;

import com.skyapi.weathernetworkapi.globalerror.GeolocatioException;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FullWeatherModelAssembler implements RepresentationModelAssembler<FullWeatherDTO, EntityModel<FullWeatherDTO>> {

    @Override
    public EntityModel<FullWeatherDTO> toModel(FullWeatherDTO dto) {
        EntityModel<FullWeatherDTO> entityModel = EntityModel.of(dto);

        try {
            entityModel.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIpAddress(null)).withSelfRel());
        } catch (GeolocatioException e) {
            throw new RuntimeException(e);
        }
        return entityModel;
    }
}
