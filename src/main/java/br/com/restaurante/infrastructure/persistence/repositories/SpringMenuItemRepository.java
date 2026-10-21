package br.com.restaurante.infrastructure.persistence.repositories;

import br.com.restaurante.infrastructure.persistence.entities.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringMenuItemRepository extends JpaRepository<MenuItemEntity, Long> {

    List<MenuItemEntity> findByRestaurantId(Long restaurantId);
}