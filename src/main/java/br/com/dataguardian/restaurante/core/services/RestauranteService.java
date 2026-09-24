package br.com.dataguardian.restaurante.core.services;

import br.com.dataguardian.restaurante.application.dto.RestauranteDtoRequest;
import br.com.dataguardian.restaurante.application.dto.RestauranteDtoResponse;
import br.com.dataguardian.restaurante.application.dto.mapper.RestauranteConverter;
import br.com.dataguardian.restaurante.application.usecases.RestauranteUseCase;
import br.com.dataguardian.restaurante.application.ports.out.RestauranteRepository;
import br.com.dataguardian.restaurante.core.domain.Restaurante;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestauranteService implements RestauranteUseCase {

    private final RestauranteRepository restauranteRepository;
    private final RestauranteConverter restauranteConverter;


    @Override
    public RestauranteDtoResponse salvarRestaurante(RestauranteDtoRequest restauranteDtoRequest) {
        validarRestaurante(restauranteDtoRequest);
        Restaurante restaurante = restauranteConverter.paraRestaurante(restauranteDtoRequest);
        Restaurante restauranteSalvo = restauranteRepository.salvar(restaurante);

        return restauranteConverter.paraRestauranteDtoResponse(restauranteSalvo);
    }

    private void validarRestaurante(RestauranteDtoRequest restauranteDtoRequest){

        if (restauranteDtoRequest == null){
            throw new IllegalArgumentException("Os dados são obrigatórios!");
        }

        validarCampoObrigatorio(restauranteDtoRequest.nome(), "Nome");
        validarCampoObrigatorio(restauranteDtoRequest.endereco(), "Endereço");
        validarCampoObrigatorio(restauranteDtoRequest.gastronomia(), "Gastronomia");
        validarCampoObrigatorio(restauranteDtoRequest.horarioFuncionamento(), "Horário de Funcionamento");

        /*if (restauranteDtoRequest.donoId() == null || restauranteDtoRequest.donoId() <=0){
            throw new IllegalArgumentException("Identificador do dono é obrigatório e deve ser maior que zero");
        }*/
    }

    private void validarCampoObrigatorio(String valor, String campo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
    }


}
