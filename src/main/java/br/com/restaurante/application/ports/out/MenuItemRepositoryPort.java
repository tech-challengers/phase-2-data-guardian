package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepositoryPort {

    MenuItem save(MenuItem item);

    Optional<MenuItem> findById(Long id);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    void deleteById(Long id);
}