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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MenuItemPersistenceAdapterTest {

    private static final UUID ID = UUID.fromString("87c9ef1c-fb4a-4d7e-898d-a48df32e84d3");
    private SpringMenuItemRepository repository;
    private MenuItemPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringMenuItemRepository.class);
        adapter = new MenuItemPersistenceAdapter(repository);
    }

    private MenuItemEntity entity() {
        return MenuItemEntity.builder()
                .id(ID)
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

        assertEquals(ID, saved.getId());
        assertEquals(10L, saved.getRestaurantId());
        assertEquals("Feijoada", saved.getName());
        assertEquals("Completa", saved.getDescription());
        assertEquals(new BigDecimal("49.90"), saved.getPrice());
        assertTrue(saved.isDineInOnly());
        assertEquals("/img/f.jpg", saved.getPhotoPath());
    }

    @Test
    void findById_found() {
        when(repository.findById(ID)).thenReturn(Optional.of(entity()));

        Optional<MenuItem> found = adapter.findById(ID);

        assertTrue(found.isPresent());
        assertEquals("Feijoada", found.get().getName());
        assertEquals(10L, found.get().getRestaurantId());
    }

    @Test
    void findById_notFound() {
        UUID id = UUID.fromString("e3682907-aec1-4b0a-ac6b-5f37a6eb8b16");
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertTrue(adapter.findById(id).isEmpty());
    }

    @Test
    void findByRestaurantId_returnsMappedList() {
        long restaurantId = 10L;
        when(repository.findByRestaurantId(restaurantId)).thenReturn(List.of(entity()));

        List<MenuItem> result = adapter.findByRestaurantId(restaurantId);

        assertEquals(1, result.size());
        assertEquals(ID, result.getFirst().getId());
    }

    @Test
    void findByRestaurantId_empty() {
        when(repository.findByRestaurantId(10L)).thenReturn(List.of());

        assertTrue(adapter.findByRestaurantId(10L).isEmpty());
    }

    @Test
    void deleteById_delegatesToRepository() {
        adapter.deleteById(ID);

        verify(repository).deleteById(ID);
    }
}