package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.Restaurante;

import java.util.List;
import java.util.Optional;

public interface RestauranteRepositoryPort {

    Restaurante save(Restaurante restaurante);

    Optional<Restaurante> findById(Long id);

    List<Restaurante> findAll();

    void deleteById(Long id);
}
