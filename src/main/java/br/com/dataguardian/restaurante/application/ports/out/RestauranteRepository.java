package br.com.dataguardian.restaurante.application.ports.out;

import br.com.dataguardian.restaurante.core.domain.Restaurante;
import java.util.Optional;

public interface RestauranteRepository {

    Restaurante salvar(Restaurante restaurante);

    Optional<Restaurante> buscarPorId(Long id);
}
