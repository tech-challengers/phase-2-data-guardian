package br.com.restaurante.infrastructure.web.dto.mapper;

import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestauranteEntityConverter {

    RestauranteEntity paraEntity(Restaurante restaurante);

    Restaurante paraDominio(RestauranteEntity restauranteEntity);

}
