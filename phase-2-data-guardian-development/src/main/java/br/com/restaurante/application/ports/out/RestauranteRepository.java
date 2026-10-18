package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.Restaurante;
import java.util.Optional;

public interface RestauranteRepository {

    Restaurante save(Restaurante restaurante);

    Optional<Restaurante> findById(Long id);
}
