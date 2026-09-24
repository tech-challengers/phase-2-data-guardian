package br.com.dataguardian.restaurante.application.usecases;

import br.com.dataguardian.restaurante.application.dto.RestauranteDtoRequest;
import br.com.dataguardian.restaurante.application.dto.RestauranteDtoResponse;

public interface RestauranteUseCase {

    RestauranteDtoResponse salvarRestaurante(
            RestauranteDtoRequest restauranteDtoRequest);
}
