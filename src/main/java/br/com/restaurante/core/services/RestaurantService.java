package br.com.restaurante.core.services;

import br.com.restaurante.core.domain.AuthenticatedUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantUseCase {

    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final RestauranteConverter restauranteConverter;


    @Override
    public RestauranteResponse saveRestaurant(RestauranteRequest request) {
        validarRestaurante(request);

        AuthenticatedUser usuarioAutenticado = getAuthenticatedUser();
        validateOwnerCreate(usuarioAutenticado);

        Restaurante restaurante = restauranteConverter.paraRestaurante(request);

        // Vincula o restaurante ao proprietário autenticado
        restaurante.setDonoId(usuarioAutenticado.getId());

        Restaurante restauranteSalvo = restauranteRepositoryPort.save(restaurante);

        return restauranteConverter.paraResponse(restauranteSalvo);
    }

    @Transactional
    public RestauranteResponse updateRestaurant(Long id, RestauranteRequest request) {

        Restaurante restaurante = restauranteRepositoryPort.findById(id)
                .orElseThrow(() -> new AccessDeniedException("Restaurant not found"));

        validarRestaurante(request);

        AuthenticatedUser usuarioAutenticado = getAuthenticatedUser();
        validateOwner(usuarioAutenticado, restaurante);

        restaurante.setNome(request.getNome());
        restaurante.setEndereco(request.getEndereco());
        restaurante.setGastronomia(request.getGastronomia());
        restaurante.setHorarioFuncionamento(request.getHorarioFuncionamento());

        return restauranteConverter.paraResponse(restauranteRepositoryPort.save(restaurante));
    }

    public RestauranteResponse findById(Long id) {
        Restaurante restaurante = restauranteRepositoryPort.findById(id).orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        return restauranteConverter.paraResponse(restaurante);
    }

    public List<RestauranteResponse> findAllRestaurant() {
        return restauranteRepositoryPort.findAll().stream().map(restauranteConverter::paraResponse).toList();
    }

    @Transactional
    public void deleteRestaurante(Long id) {

        Restaurante restaurante = restauranteRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        AuthenticatedUser usuarioAutenticado = getAuthenticatedUser();

        validateOwner(usuarioAutenticado, restaurante);

        restauranteRepositoryPort.deleteById(id);
    }

    private void validarRestaurante(RestauranteRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("The data is mandatory!");
        }

        validarCampoObrigatorio(request.getNome(), "Nome");
        validarCampoObrigatorio(request.getEndereco(), "Endereço");
        validarCampoObrigatorio(request.getGastronomia(), "Gastronomia");
        validarCampoObrigatorio(request.getHorarioFuncionamento(), "Horário de Funcionamento");

    }

    private void validarCampoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " is mandatory");
        }
    }

    private void validateOwner(AuthenticatedUser user, Restaurante restaurante) {

        if (user == null || !user.isDonoDeRestaurante()){
            throw new AccessDeniedException("You dont have permission to manage restaurants!");
        }

        if (!Objects.equals(user.getId(), restaurante.getDonoId())) {
            throw new AccessDeniedException("The restaurant doesnt belong to you.");
        }
    }

    private void validateOwnerCreate(AuthenticatedUser user){

        if (user == null || !user.isDonoDeRestaurante()){
            throw new AccessDeniedException("You dont have permission to manage restaurants!");
        }
    }

    private AuthenticatedUser getAuthenticatedUser() {
        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedException("User not authenticated.");
        }

        return user;
    }


}
