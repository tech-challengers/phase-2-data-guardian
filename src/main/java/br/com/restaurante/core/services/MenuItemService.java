package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.application.ports.out.MenuItemRepositoryPort;
import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;

import java.util.List;

public class MenuItemService implements MenuItemUseCase {

    private final MenuItemRepositoryPort menuItemRepositoryPort;
    private final RestauranteRepositoryPort restauranteRepositoryPort;

    public MenuItemService(MenuItemRepositoryPort menuItemRepositoryPort,
                           RestauranteRepositoryPort restauranteRepositoryPort) {
        this.menuItemRepositoryPort = menuItemRepositoryPort;
        this.restauranteRepositoryPort = restauranteRepositoryPort;
    }

    @Override
    public MenuItem create(Long userId, Long restaurantId, MenuItem item) {
        checkOwner(userId, restaurantId);
        checkNameAvailable(restaurantId, item.getName(), null);
        return menuItemRepositoryPort.save(item);
    }

    @Override
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return menuItemRepositoryPort.findByRestaurantId(restaurantId);
    }

    @Override
    public MenuItem findById(Long id) {
        return menuItemRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found"));
    }

    @Override
    public MenuItem update(Long userId, Long id, MenuItem data) {
        MenuItem item = findById(id);
        checkOwner(userId, item.getRestaurantId());
        checkNameAvailable(item.getRestaurantId(), data.getName(), id);
        item.update(data.getName(), data.getDescription(), data.getPrice(),
                data.isDineInOnly(), data.getPhotoPath());
        return menuItemRepositoryPort.save(item);
    }

    @Override
    public void delete(Long userId, Long id) {
        MenuItem item = findById(id);
        checkOwner(userId, item.getRestaurantId());
        menuItemRepositoryPort.deleteById(id);
    }

    private void checkOwner(Long userId, Long restaurantId) {
        Restaurante restaurante = restauranteRepositoryPort.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        if (!restaurante.getDonoId().equals(userId)) {
            throw new AccessDeniedDomainException("Only the restaurant owner can manage its menu");
        }
    }

    private void checkNameAvailable(Long restaurantId, String name, Long ignoreItemId) {
        if (name == null) {
            return; 
        }
        menuItemRepositoryPort.findByRestaurantIdAndName(restaurantId, name.trim())
                .filter(existing -> !existing.getId().equals(ignoreItemId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "A menu item with this name already exists in this restaurant");
                });
    }
}