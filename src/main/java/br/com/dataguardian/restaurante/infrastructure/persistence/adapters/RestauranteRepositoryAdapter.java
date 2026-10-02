package br.com.dataguardian.restaurante.infrastructure.persistence.adapters;

import br.com.dataguardian.restaurante.application.ports.out.RestauranteRepository;
import br.com.dataguardian.restaurante.core.domain.Restaurante;
import br.com.dataguardian.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import br.com.dataguardian.restaurante.infrastructure.persistence.repositories.RestauranteJpaRepository;
import br.com.dataguardian.restaurante.infrastructure.web.dto.mapper.RestauranteEntityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestauranteRepositoryAdapter implements RestauranteRepository {

    private final RestauranteJpaRepository restauranteJpaRepository;
    private final RestauranteEntityConverter restauranteEntityConverter;

    @Override
    public Optional<Restaurante> buscarPorId(Long id) {
        return restauranteJpaRepository.findById(id)
                .map(restauranteEntityConverter::paraDominio);
    }

    @Override
    public Restaurante salvar(Restaurante restaurante) {
        RestauranteEntity entity = restauranteEntityConverter.paraEntity(restaurante);

        RestauranteEntity entitySalva = restauranteJpaRepository.save(entity);

        return restauranteEntityConverter.paraDominio(entitySalva);
    }
}
