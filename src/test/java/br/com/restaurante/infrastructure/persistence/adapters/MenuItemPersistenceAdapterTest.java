package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.infrastructure.persistence.entities.MenuItemEntity;
import br.com.restaurante.infrastructure.persistence.repositories.SpringMenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MenuItemPersistenceAdapterTest {

    private SpringMenuItemRepository repository;
    private MenuItemPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringMenuItemRepository.class);
        adapter = new MenuItemPersistenceAdapter(repository);
    }

    private MenuItemEntity entity() {
        return MenuItemEntity.builder()
                .id(100L)
                .restaurantId(10L)
                .name("Feijoada")
                .description("Completa")
                .price(new BigDecimal("49.90"))
                .dineInOnly(true)
                .photoPath("/img/f.jpg")
                .build();
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        MenuItem item = new MenuItem(null, 10L, "Feijoada", "Completa",
                new BigDecimal("49.90"), true, "/img/f.jpg");
        when(repository.save(any(MenuItemEntity.class))).thenReturn(entity());

        MenuItem saved = adapter.save(item);

        ArgumentCaptor<MenuItemEntity> captor = ArgumentCaptor.forClass(MenuItemEntity.class);
        verify(repository).save(captor.capture());
        MenuItemEntity sent = captor.getValue();
        assertNull(sent.getId());
        assertEquals(10L, sent.getRestaurantId());
        assertEquals("Feijoada", sent.getName());
        assertEquals("Completa", sent.getDescription());
        assertEquals(new BigDecimal("49.90"), sent.getPrice());
        assertTrue(sent.isDineInOnly());
        assertEquals("/img/f.jpg", sent.getPhotoPath());

        assertEquals(100L, saved.getId());
        assertEquals(10L, saved.getRestaurantId());
        assertEquals("Feijoada", saved.getName());
        assertEquals("Completa", saved.getDescription());
        assertEquals(new BigDecimal("49.90"), saved.getPrice());
        assertTrue(saved.isDineInOnly());
        assertEquals("/img/f.jpg", saved.getPhotoPath());
    }

    @Test
    void findById_found() {
        when(repository.findById(100L)).thenReturn(Optional.of(entity()));

        Optional<MenuItem> found = adapter.findById(100L);

        assertTrue(found.isPresent());
        assertEquals("Feijoada", found.get().getName());
        assertEquals(10L, found.get().getRestaurantId());
    }

    @Test
    void findById_notFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertTrue(adapter.findById(999L).isEmpty());
    }

    @Test
    void findByRestaurantId_returnsMappedList() {
        when(repository.findByRestaurantId(10L)).thenReturn(List.of(entity()));

        List<MenuItem> result = adapter.findByRestaurantId(10L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void findByRestaurantId_empty() {
        when(repository.findByRestaurantId(10L)).thenReturn(List.of());

        assertTrue(adapter.findByRestaurantId(10L).isEmpty());
    }

    @Test
    void deleteById_delegatesToRepository() {
        adapter.deleteById(100L);

        verify(repository).deleteById(100L);
    }
}