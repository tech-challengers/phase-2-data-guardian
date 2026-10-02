package br.com.dataguardian.restaurante.infrastructure.web.dto.mapper;

import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteResponse;
import br.com.dataguardian.restaurante.core.domain.Restaurante;
import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestauranteConverter {

    @Mapping(target = "id", ignore = true)
    Restaurante paraRestaurante(RestauranteRequest request);

    RestauranteResponse paraResponse(Restaurante restaurante);
}
