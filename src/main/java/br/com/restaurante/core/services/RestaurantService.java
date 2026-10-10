package br.com.restaurante.core.services;
import br.com.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.restaurante.infrastructure.web.dto.RestauranteResponse;
import br.com.restaurante.infrastructure.web.dto.mapper.RestauranteConverter;
import br.com.restaurante.application.usecases.RestaurantUseCase;
import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.core.domain.Restaurante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantUseCase {

    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final RestauranteConverter restauranteConverter;


    @Override
    public RestauranteResponse saveRestaurant(RestauranteRequest request) {
        validarRestaurante(request);

        Restaurante restaurante = restauranteConverter.paraRestaurante(request);
        Restaurante restauranteSalvo = restauranteRepositoryPort.save(restaurante);

        return restauranteConverter.paraResponse(restauranteSalvo);
    }

    @Transactional
    public RestauranteResponse updateRestaurant(Long id, RestauranteRequest request) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("The restaurant identifier must be greater than zero.");
        }
        validarRestaurante(request);

        Restaurante restaurante = restauranteRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        restaurante.setNome(request.getNome());
        restaurante.setEndereco(request.getEndereco());
        restaurante.setGastronomia(request.getGastronomia());
        restaurante.setHorarioFuncionamento(request.getHorarioFuncionamento());
        restaurante.setDonoId(request.getDonoId());

        return restauranteConverter.paraResponse(restauranteRepositoryPort.save(restaurante));
    }

    public RestauranteResponse findById(Long id){
        Restaurante restaurante = restauranteRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        return  restauranteConverter.paraResponse(restaurante);
    }

    public List<RestauranteResponse> findAllRestaurant(){
        return restauranteRepositoryPort.findAll()
                .stream()
                .map(restauranteConverter::paraResponse)
                .toList();
    }

    public void deleterestaurante(Long id){
        restauranteRepositoryPort.deleteById(id);
    }

    private void validarRestaurante(RestauranteRequest request){

        if (request == null){
            throw new IllegalArgumentException("The data is mandatory!");
        }

        validarCampoObrigatorio(request.getNome(), "Nome");
        validarCampoObrigatorio(request.getEndereco(), "Endereço");
        validarCampoObrigatorio(request.getGastronomia(), "Gastronomia");
        validarCampoObrigatorio(request.getHorarioFuncionamento(), "Horário de Funcionamento");

        if (request.getDonoId() == null || request.getDonoId() <= 0){
            throw new IllegalArgumentException("The restaurant identifier must be greater than zero.");
        }
    }

    private void validarCampoObrigatorio(String valor, String campo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException(campo + " is mandatory");
        }
    }


}
