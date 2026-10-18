package br.com.restaurante.application.usecases;

import br.com.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.restaurante.infrastructure.web.dto.RestauranteResponse;

public interface RestaurantUseCase {

    RestauranteResponse saveRestaurant(RestauranteRequest request);

    RestauranteResponse updateRestaurant(Long id, RestauranteRequest request);
}
