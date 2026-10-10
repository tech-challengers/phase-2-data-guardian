package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import br.com.restaurante.infrastructure.persistence.repositories.RestauranteJpaRepository;
import br.com.restaurante.infrastructure.web.dto.mapper.RestauranteEntityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestauranteRepositoryPortAdapter implements RestauranteRepositoryPort {

    private final RestauranteJpaRepository restauranteJpaRepository;
    private final RestauranteEntityConverter restauranteEntityConverter;

    @Override
    public Optional<Restaurante> findById(Long id) {
        return restauranteJpaRepository.findById(id)
                .map(restauranteEntityConverter::paraDominio);
    }

    @Override
    public List<Restaurante> findAll() {
        return restauranteJpaRepository.findAll()
                .stream()
                .map(restauranteEntityConverter::paraDominio)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        restauranteJpaRepository.deleteById(id);
    }

    @Override
    public Restaurante save(Restaurante restaurante) {
        RestauranteEntity entity = restauranteEntityConverter.paraEntity(restaurante);

        RestauranteEntity entitySalva = restauranteJpaRepository.save(entity);

        return restauranteEntityConverter.paraDominio(entitySalva);
    }
}
