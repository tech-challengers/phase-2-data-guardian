package br.com.dataguardian.restaurante.infrastructure.web.controllers;

import br.com.dataguardian.restaurante.core.services.RestauranteService;
import br.com.restaurante.infrastructure.web.controllers.RestauranteApi;
import br.com.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.restaurante.infrastructure.web.dto.RestauranteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestauranteController implements RestauranteApi {

    private final RestauranteService restauranteService;

    @Override
    public ResponseEntity<RestauranteResponse> atualizarRestaurante(Long id, RestauranteRequest restauranteRequest) {
        return ResponseEntity.ok(restauranteService.atualizarRestaurante(id, restauranteRequest));
    }

    @Override
    public ResponseEntity<RestauranteResponse> criarRestaurante(RestauranteRequest restauranteRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restauranteService.salvarRestaurante(restauranteRequest));
    }
}
