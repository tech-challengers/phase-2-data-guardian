package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.application.ports.out.RestauranteRepository;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import br.com.restaurante.infrastructure.persistence.repositories.RestauranteJpaRepository;
import br.com.restaurante.infrastructure.web.dto.mapper.RestauranteEntityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestauranteRepositoryAdapter implements RestauranteRepository {

    private final RestauranteJpaRepository restauranteJpaRepository;
    private final RestauranteEntityConverter restauranteEntityConverter;

    @Override
    public Optional<Restaurante> findById(Long id) {
        return restauranteJpaRepository.findById(id)
                .map(restauranteEntityConverter::paraDominio);
    }

    @Override
    public Restaurante save(Restaurante restaurante) {
        RestauranteEntity entity = restauranteEntityConverter.paraEntity(restaurante);

        RestauranteEntity entitySalva = restauranteJpaRepository.save(entity);

        return restauranteEntityConverter.paraDominio(entitySalva);
    }
}
