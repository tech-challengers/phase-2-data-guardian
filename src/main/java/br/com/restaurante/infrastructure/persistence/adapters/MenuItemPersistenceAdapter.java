package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.application.ports.out.MenuItemRepositoryPort;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.infrastructure.persistence.entities.MenuItemEntity;
import br.com.restaurante.infrastructure.persistence.repositories.SpringMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuItemPersistenceAdapter implements MenuItemRepositoryPort {

    private final SpringMenuItemRepository repository;

    @Override
    public MenuItem save(MenuItem item) {
        return toDomain(repository.save(toEntity(item)));
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return repository.findByRestaurantId(restaurantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<MenuItem> findByRestaurantIdAndName(Long restaurantId, String name) {
        return repository.findByRestaurantIdAndNameIgnoreCase(restaurantId, name)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private MenuItem toDomain(MenuItemEntity entity) {
        return new MenuItem(entity.getId(), entity.getRestaurantId(), entity.getName(),
                entity.getDescription(), entity.getPrice(), entity.isDineInOnly(),
                entity.getPhotoPath());
    }

    private MenuItemEntity toEntity(MenuItem item) {
        return MenuItemEntity.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurantId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .dineInOnly(item.isDineInOnly())
                .photoPath(item.getPhotoPath())
                .build();
    }
}