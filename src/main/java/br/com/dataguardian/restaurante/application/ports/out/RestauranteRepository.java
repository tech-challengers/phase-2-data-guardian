package br.com.dataguardian.restaurante.application.ports.out;

import br.com.dataguardian.restaurante.core.domain.Restaurante;

public interface RestauranteRepository {

    Restaurante salvar(Restaurante restaurante);
}
