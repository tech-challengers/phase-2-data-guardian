package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.core.services.RestaurantService;
import br.com.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.restaurante.infrastructure.web.dto.RestauranteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestauranteController implements RestauranteApi {

    private final RestaurantService restaurantService;

    @Override
    public ResponseEntity<RestauranteResponse> updateRestaurante(Long id, RestauranteRequest restauranteRequest) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restauranteRequest));
    }

    @Override
    public ResponseEntity<RestauranteResponse> criarRestaurante(RestauranteRequest restauranteRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.saveRestaurant(restauranteRequest));
    }

    @Override
    public ResponseEntity<RestauranteResponse> findRestauranteById(Long id){

        return ResponseEntity.ok(restaurantService.findById(id));
    }

    @Override
    public ResponseEntity<List<RestauranteResponse>> findAllRestaurantes(){

        return ResponseEntity.ok(restaurantService.findAllRestaurant());
    }

    public ResponseEntity<Void> deleteRestaurante(Long id){
        restaurantService.deleterestaurante(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
