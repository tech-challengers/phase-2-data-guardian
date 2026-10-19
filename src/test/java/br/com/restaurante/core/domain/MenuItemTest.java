package br.com.restaurante.core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {

    private MenuItem valid() {
        return new MenuItem(1L, 10L, "Feijoada", "Completa",
                new BigDecimal("49.90"), false, "/img/f.jpg");
    }

    @Test
    void shouldCreateValidItem() {
        MenuItem item = valid();

        assertEquals(1L, item.getId());
        assertEquals(10L, item.getRestaurantId());
        assertEquals("Feijoada", item.getName());
        assertEquals("Completa", item.getDescription());
        assertEquals(new BigDecimal("49.90"), item.getPrice());
        assertFalse(item.isDineInOnly());
        assertEquals("/img/f.jpg", item.getPhotoPath());
    }

    @Test
    void shouldAcceptZeroPrice() {
        assertDoesNotThrow(() ->
                new MenuItem(null, 10L, "Brinde", null, BigDecimal.ZERO, true, null));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () ->
                new MenuItem(null, 10L, null, "d", BigDecimal.ONE, false, null));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new MenuItem(null, 10L, "   ", "d", BigDecimal.ONE, false, null));
    }

    @Test
    void shouldRejectNullPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new MenuItem(null, 10L, "Nome", "d", null, false, null));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                new MenuItem(null, 10L, "Nome", "d", new BigDecimal("-1"), false, null));
    }

    @Test
    void shouldUpdateFields() {
        MenuItem item = valid();

        item.update("Moqueca", "Capixaba", new BigDecimal("59.00"), true, "/img/m.jpg");

        assertEquals("Moqueca", item.getName());
        assertEquals("Capixaba", item.getDescription());
        assertEquals(new BigDecimal("59.00"), item.getPrice());
        assertTrue(item.isDineInOnly());
        assertEquals("/img/m.jpg", item.getPhotoPath());
        assertEquals(10L, item.getRestaurantId());
    }

    @Test
    void shouldRejectInvalidUpdateAndKeepOldValues() {
        MenuItem item = valid();

        assertThrows(IllegalArgumentException.class, () ->
                item.update("", "d", BigDecimal.ONE, false, null));
        assertThrows(IllegalArgumentException.class, () ->
                item.update("Ok", "d", new BigDecimal("-5"), false, null));

        assertEquals("Feijoada", item.getName());
        assertEquals(new BigDecimal("49.90"), item.getPrice());
    }
}