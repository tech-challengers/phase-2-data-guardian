package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.MenuItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepositoryPort {

    MenuItem save(MenuItem item);

    Optional<MenuItem> findById(UUID id);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    void deleteById(UUID id);

    void existsByName(String name, Long restaurantId);
}