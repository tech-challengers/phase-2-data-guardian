package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.out.MenuItemRepositoryPort;
import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long ITEM_ID = 100L;

    @Mock
    private MenuItemRepositoryPort menuItemRepositoryPort;

    @Mock
    private RestauranteRepositoryPort restauranteRepositoryPort;

    @InjectMocks
    private MenuItemService service;

    private Restaurante restaurante;
    private MenuItem existing;

    @BeforeEach
    void setUp() {
        restaurante = new Restaurante(RESTAURANT_ID, "Praça", "Rua 1", "Brasileira", "11-22", OWNER_ID);
        existing = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Completa",
                new BigDecimal("49.90"), false, "/img/f.jpg");
    }

    private MenuItem newItem() {
        return new MenuItem(null, RESTAURANT_ID, "Moqueca", "Capixaba",
                new BigDecimal("59.00"), true, "/img/m.jpg");
    }

    // create

    @Test
    void createShouldSaveWhenUserIsOwner() {
        MenuItem item = newItem();
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(item)).thenReturn(item);

        MenuItem result = service.create(OWNER_ID, RESTAURANT_ID, item);

        assertSame(item, result);
        verify(menuItemRepositoryPort).save(item);
    }

    @Test
    void createShouldDenyWhenUserIsNotOwner() {
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.create(OTHER_USER_ID, RESTAURANT_ID, newItem()));

        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void createShouldFailWhenRestaurantNotFound() {
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(OWNER_ID, RESTAURANT_ID, newItem()));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void createShouldFailWhenNameAlreadyExistsInRestaurant() {
        MenuItem duplicate = new MenuItem(200L, RESTAURANT_ID, "Moqueca", "Outra",
                new BigDecimal("10.00"), false, null);
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.findByRestaurantIdAndName(RESTAURANT_ID, "Moqueca"))
                .thenReturn(Optional.of(duplicate));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(OWNER_ID, RESTAURANT_ID, newItem()));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void createShouldIgnoreSurroundingSpacesWhenCheckingDuplicateName() {
        MenuItem withSpaces = new MenuItem(null, RESTAURANT_ID, "  Moqueca  ", "Capixaba",
                new BigDecimal("59.00"), true, null);
        MenuItem duplicate = new MenuItem(200L, RESTAURANT_ID, "Moqueca", "Outra",
                new BigDecimal("10.00"), false, null);
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.findByRestaurantIdAndName(RESTAURANT_ID, "Moqueca"))
                .thenReturn(Optional.of(duplicate));

        assertThrows(IllegalArgumentException.class,
                () -> service.create(OWNER_ID, RESTAURANT_ID, withSpaces));

        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void createShouldSaveWhenNameIsFreeInThisRestaurant() {
        MenuItem item = newItem();
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.findByRestaurantIdAndName(RESTAURANT_ID, "Moqueca"))
                .thenReturn(Optional.empty());
        when(menuItemRepositoryPort.save(item)).thenReturn(item);

        assertSame(item, service.create(OWNER_ID, RESTAURANT_ID, item));

        verify(menuItemRepositoryPort).findByRestaurantIdAndName(RESTAURANT_ID, "Moqueca");
        verify(menuItemRepositoryPort).save(item);
    }

    // read

    @Test
    void findByRestaurantIdShouldReturnItems() {
        when(menuItemRepositoryPort.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of(existing));

        List<MenuItem> result = service.findByRestaurantId(RESTAURANT_ID);

        assertEquals(1, result.size());
        assertSame(existing, result.get(0));
    }

    @Test
    void findByRestaurantIdShouldReturnEmptyList() {
        when(menuItemRepositoryPort.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of());

        assertTrue(service.findByRestaurantId(RESTAURANT_ID).isEmpty());
    }

    @Test
    void findByIdShouldReturnItem() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));

        assertSame(existing, service.findById(ITEM_ID));
    }

    @Test
    void findByIdShouldFailWhenNotFound() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.findById(ITEM_ID));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // update

    @Test
    void updateShouldChangeFieldsWhenUserIsOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.update(OWNER_ID, ITEM_ID, newItem());

        assertEquals("Moqueca", result.getName());
        assertEquals(new BigDecimal("59.00"), result.getPrice());
        assertTrue(result.isDineInOnly());
        assertEquals(RESTAURANT_ID, result.getRestaurantId());
        verify(menuItemRepositoryPort).save(existing);
    }

    @Test
    void updateShouldDenyWhenUserIsNotOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.update(OTHER_USER_ID, ITEM_ID, newItem()));

        assertEquals("Feijoada", existing.getName());
        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void updateShouldFailWhenItemNotFound() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(OWNER_ID, ITEM_ID, newItem()));

        verify(restauranteRepositoryPort, never()).findById(any());
        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void updateShouldFailWhenRestaurantNotFound() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.update(OWNER_ID, ITEM_ID, newItem()));

        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void updateShouldFailWhenNameBelongsToAnotherItem() {
        MenuItem other = new MenuItem(200L, RESTAURANT_ID, "Moqueca", "Outra",
                new BigDecimal("10.00"), false, null);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.findByRestaurantIdAndName(RESTAURANT_ID, "Moqueca"))
                .thenReturn(Optional.of(other));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.update(OWNER_ID, ITEM_ID, newItem()));

        assertTrue(ex.getMessage().contains("already exists"));
        assertEquals("Feijoada", existing.getName());
        verify(menuItemRepositoryPort, never()).save(any());
    }

    @Test
    void updateShouldAllowKeepingTheSameName() {
        MenuItem sameName = new MenuItem(null, RESTAURANT_ID, "Feijoada", "Nova descrição",
                new BigDecimal("55.00"), false, null);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.findByRestaurantIdAndName(RESTAURANT_ID, "Feijoada"))
                .thenReturn(Optional.of(existing));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.update(OWNER_ID, ITEM_ID, sameName);

        assertEquals("Feijoada", result.getName());
        assertEquals(new BigDecimal("55.00"), result.getPrice());
        verify(menuItemRepositoryPort).save(existing);
    }

    // delete

    @Test
    void deleteShouldRemoveWhenUserIsOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        service.delete(OWNER_ID, ITEM_ID);

        verify(menuItemRepositoryPort).deleteById(ITEM_ID);
    }

    @Test
    void deleteShouldDenyWhenUserIsNotOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.delete(OTHER_USER_ID, ITEM_ID));

        verify(menuItemRepositoryPort, never()).deleteById(any());
    }

    @Test
    void deleteShouldFailWhenItemNotFound() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.delete(OWNER_ID, ITEM_ID));

        verify(menuItemRepositoryPort, never()).deleteById(any());
    }
}