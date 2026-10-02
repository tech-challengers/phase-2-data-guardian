package br.com.dataguardian.restaurante.infrastructure.persistence.repositories;

import br.com.dataguardian.restaurante.infrastructure.persistence.entities.RestauranteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteJpaRepository extends JpaRepository<RestauranteEntity, Long> {
}
