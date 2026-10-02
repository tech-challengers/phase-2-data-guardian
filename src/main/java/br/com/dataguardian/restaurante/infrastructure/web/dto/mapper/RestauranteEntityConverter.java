package br.com.dataguardian.restaurante.infrastructure.web.dto.mapper;

import br.com.dataguardian.restaurante.core.domain.Restaurante;
import br.com.dataguardian.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestauranteEntityConverter {

    RestauranteEntity paraEntity(Restaurante restaurante);

    Restaurante paraDominio(RestauranteEntity restauranteEntity);

}
