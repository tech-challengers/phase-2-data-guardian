package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long ITEM_ID = 100L;

    private MenuItemUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        useCase = mock(MenuItemUseCase.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MenuItemController(useCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Long id, String role) {
        AuthenticatedUser user = new AuthenticatedUser(id, "u@r.com", "Usuario", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    private MenuItem item() {
        return new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Completa",
                new BigDecimal("49.90"), false, "/img/f.jpg");
    }

    private String body(String name, String price) {
        return """
                {
                  "name": "%s",
                  "description": "Completa",
                  "price": %s,
                  "dineInOnly": false,
                  "photoPath": "/img/f.jpg"
                }
                """.formatted(name, price);
    }

    // ---------- POST ----------

    @Test
    void createShouldReturn201() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.create(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class))).thenReturn(item());

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.restaurantId").value(10))
                .andExpect(jsonPath("$.name").value("Feijoada"))
                .andExpect(jsonPath("$.price").value(49.90))
                .andExpect(jsonPath("$.dineInOnly").value(false));
    }

    @Test
    void createShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        when(useCase.create(eq(2L), eq(RESTAURANT_ID), any(MenuItem.class)))
                .thenThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"));

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createShouldReturn404WhenRestaurantNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.create(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class)))
                .thenThrow(new IllegalArgumentException("Restaurant not found"));

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createShouldReturn400WhenPriceIsNegative() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "-5")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    void createShouldReturn400WhenNameIsBlank() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "10")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    void createShouldDenyWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(useCase);
    }

    // ---------- GET ----------

    @Test
    void listShouldReturn200WithItems() throws Exception {
        when(useCase.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of(item()));

        mockMvc.perform(get("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Feijoada"));
    }

    @Test
    void listShouldReturnEmptyArray() throws Exception {
        when(useCase.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getByIdShouldReturn200() throws Exception {
        when(useCase.findById(ITEM_ID)).thenReturn(item());

        mockMvc.perform(get("/api/v1/menu-items/{id}", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.photoPath").value("/img/f.jpg"));
    }

    @Test
    void getByIdShouldReturn404WhenNotFound() throws Exception {
        when(useCase.findById(999L)).thenThrow(new IllegalArgumentException("MenuItem not found"));

        mockMvc.perform(get("/api/v1/menu-items/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT ----------

    @Test
    void updateShouldReturn200() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.update(eq(USER_ID), eq(ITEM_ID), any(MenuItem.class))).thenReturn(item());

        mockMvc.perform(put("/api/v1/menu-items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijoada"));
    }

    @Test
    void updateShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        when(useCase.update(eq(2L), eq(ITEM_ID), any(MenuItem.class)))
                .thenThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"));

        mockMvc.perform(put("/api/v1/menu-items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateShouldReturn404WhenNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.update(eq(USER_ID), eq(ITEM_ID), any(MenuItem.class)))
                .thenThrow(new IllegalArgumentException("MenuItem not found"));

        mockMvc.perform(put("/api/v1/menu-items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Feijoada", "49.90")))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE ----------

    @Test
    void deleteShouldReturn204() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");

        mockMvc.perform(delete("/api/v1/menu-items/{id}", ITEM_ID))
                .andExpect(status().isNoContent());

        verify(useCase).delete(USER_ID, ITEM_ID);
    }

    @Test
    void deleteShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        doThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"))
                .when(useCase).delete(2L, ITEM_ID);

        mockMvc.perform(delete("/api/v1/menu-items/{id}", ITEM_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteShouldReturn404WhenNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        doThrow(new IllegalArgumentException("MenuItem not found"))
                .when(useCase).delete(USER_ID, ITEM_ID);

        mockMvc.perform(delete("/api/v1/menu-items/{id}", ITEM_ID))
                .andExpect(status().isNotFound());
    }
}