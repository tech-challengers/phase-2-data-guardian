package br.com.dataguardian.restaurante.application.dto.mapper;

import br.com.dataguardian.restaurante.application.dto.RestauranteDtoRequest;
import br.com.dataguardian.restaurante.application.dto.RestauranteDtoResponse;
import br.com.dataguardian.restaurante.core.domain.Restaurante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestauranteConverter {

    @Mapping(target = "id", ignore = true)
    Restaurante paraRestaurante(RestauranteDtoRequest request);

    RestauranteDtoResponse paraRestauranteDtoResponse(Restaurante restaurante);
}
