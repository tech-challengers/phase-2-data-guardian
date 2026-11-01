package br.com.restaurante.infrastructure.persistence.repositories;

import br.com.restaurante.infrastructure.persistence.entities.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringMenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {

    List<MenuItemEntity> findByRestaurantId(Long restaurantId);

    Optional<MenuItemEntity> findByNameAndRestaurantId(String name, Long restaurantId);
}