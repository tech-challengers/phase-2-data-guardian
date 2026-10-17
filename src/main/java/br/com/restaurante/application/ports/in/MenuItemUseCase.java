package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.MenuItem;

import java.util.List;

public interface MenuItemUseCase {

    MenuItem create(Long userId, Long restaurantId, MenuItem item);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    MenuItem findById(Long id);

    MenuItem update(Long userId, Long id, MenuItem data);

    void delete(Long userId, Long id);
}