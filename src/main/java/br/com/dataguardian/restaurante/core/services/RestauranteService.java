package br.com.dataguardian.restaurante.core.services;

import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteRequest;
import br.com.dataguardian.restaurante.infrastructure.web.dto.RestauranteResponse;
import br.com.dataguardian.restaurante.infrastructure.web.dto.mapper.RestauranteConverter;
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
    public RestauranteResponse salvarRestaurante(RestauranteRequest request) {
        validarRestaurante(request);

        Restaurante restaurante = restauranteConverter.paraRestaurante(request);
        Restaurante restauranteSalvo = restauranteRepository.salvar(restaurante);

        return restauranteConverter.paraResponse(restauranteSalvo);
    }

    private void validarRestaurante(RestauranteRequest request){

        if (request == null){
            throw new IllegalArgumentException("Os dados são obrigatórios!");
        }

        validarCampoObrigatorio(request.getNome(), "Nome");
        validarCampoObrigatorio(request.getEndereco(), "Endereço");
        validarCampoObrigatorio(request.getGastronomia(), "Gastronomia");
        validarCampoObrigatorio(request.getHorarioFuncionamento(), "Horário de Funcionamento");

        if (request.getDonoId() == null || request.getDonoId() <= 0){
            throw new IllegalArgumentException("Identificador do dono é obrigatório e deve ser maior que zero");
        }
    }

    private void validarCampoObrigatorio(String valor, String campo){
        if (valor == null || valor.isBlank()){
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
    }


}
