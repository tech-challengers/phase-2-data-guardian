package br.com.dataguardian.restaurante.application.usecases;

import br.com.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.restaurante.infrastructure.web.dto.RestauranteResponse;

public interface RestauranteUseCase {

    RestauranteResponse salvarRestaurante(RestauranteRequest request);

    RestauranteResponse atualizarRestaurante(Long id, RestauranteRequest request);
}
