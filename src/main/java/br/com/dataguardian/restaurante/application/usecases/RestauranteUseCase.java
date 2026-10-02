package br.com.dataguardian.restaurante.application.usecases;

import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteResponse;

public interface RestauranteUseCase {

    RestauranteResponse salvarRestaurante(RestauranteRequest request);
}
