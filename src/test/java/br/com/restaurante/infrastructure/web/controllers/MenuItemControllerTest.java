package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final String FIREBASE_URL = "https://firebasestorage.googleapis.com/v0/b/bucket/o/menu-items%2F100%2Fphoto.jpg?alt=media";

    private MenuItemUseCase useCase;
    private MockMvc mockMvc;
    private MenuItemController controller;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        useCase = mock(MenuItemUseCase.class);
        controller = new MenuItemController(useCase, new ObjectMapper());
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
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

    private MenuItem itemWithPhoto(String photoUrl) {
        return new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Completa",
                new BigDecimal("49.90"), true, photoUrl);
    }

    private String jsonBody(String name, String price) {
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

    private String jsonBodyDineIn(String name, String price, boolean dineIn) {
        return """
                {
                  "name": "%s",
                  "description": "Completa",
                  "price": %s,
                  "dineInOnly": %s,
                  "photoPath": "/img/f.jpg"
                }
                """.formatted(name, price, dineIn);
    }

    // =========================================================================
    // ENDPOINT 1: POST /api/v1/restaurants/{id}/menu-items (JSON com URL)
    // =========================================================================

    @Test
    @DisplayName("createMenuItem via JSON com URL deve retornar 201")
    void createMenuItemJsonShouldReturn201() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.create(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class))).thenReturn(item());

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.restaurantId").value(10))
                .andExpect(jsonPath("$.name").value("Feijoada"))
                .andExpect(jsonPath("$.price").value(49.90))
                .andExpect(jsonPath("$.photoPath").value("/img/f.jpg"));
    }

    @Test
    @DisplayName("createMenuItem via JSON com dineInOnly true deve mapear corretamente")
    void createMenuItemJsonWithDineInOnlyTrue() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MenuItem dineInItem = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Completa",
                new BigDecimal("49.90"), true, "/img/f.jpg");
        when(useCase.create(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class))).thenReturn(dineInItem);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBodyDineIn("Feijoada", "49.90", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dineInOnly").value(true));
    }

    @Test
    @DisplayName("createMenuItem via JSON deve retornar 403 quando não for dono")
    void createMenuItemJsonShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        when(useCase.create(eq(2L), eq(RESTAURANT_ID), any(MenuItem.class)))
                .thenThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"));

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createMenuItem via JSON deve retornar 404 quando restaurante não existir")
    void createMenuItemJsonShouldReturn404WhenRestaurantNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.create(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class)))
                .thenThrow(new IllegalArgumentException("Restaurant not found"));

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("createMenuItem via JSON deve retornar 400 quando preço for negativo")
    void createMenuItemJsonShouldReturn400WhenPriceIsNegative() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "-5")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("createMenuItem via JSON deve retornar 400 quando nome for vazio")
    void createMenuItemJsonShouldReturn400WhenNameIsBlank() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("   ", "10")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("createMenuItem via JSON deve retornar 403 quando não autenticado")
    void createMenuItemJsonShouldDenyWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/restaurants/{id}/menu-items", RESTAURANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(useCase);
    }

    // =========================================================================
    // ENDPOINT 2: POST /api/v1/restaurants/{id}/menu-items/with-photo (Multipart)
    // =========================================================================

    @Test
    @DisplayName("createMenuItemWithPhoto via Multipart deve retornar 201 com URL da imagem no Firebase")
    void createMenuItemWithPhotoShouldReturn201() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());
        String data = jsonBody("Feijoada", "49.90");

        when(useCase.createWithPhoto(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class), any(MultipartFile.class)))
                .thenReturn(itemWithPhoto(FIREBASE_URL));

        mockMvc.perform(multipart("/api/v1/restaurants/{id}/menu-items/with-photo", RESTAURANT_ID)
                        .file(photo)
                        .param("data", data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.photoPath").value(FIREBASE_URL));
    }

    @Test
    @DisplayName("createMenuItemWithPhoto deve retornar 400 quando campo data for ausente ou vazio")
    void createMenuItemWithPhotoShouldReturn400WhenDataIsBlank() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/restaurants/{id}/menu-items/with-photo", RESTAURANT_ID)
                        .file(photo)
                        .param("data", "   "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("createMenuItemWithPhoto deve lançar IllegalArgumentException quando data for null")
    void createMenuItemWithPhotoNullDataDirectCall() {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> controller.createMenuItemWithPhoto(RESTAURANT_ID, null, photo));
    }

    @Test
    @DisplayName("createMenuItemWithPhoto deve retornar 400 quando campo data for JSON inválido")
    void createMenuItemWithPhotoShouldReturn400WhenDataIsInvalidJson() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/restaurants/{id}/menu-items/with-photo", RESTAURANT_ID)
                        .file(photo)
                        .param("data", "{invalid-json}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("createMenuItemWithPhoto deve retornar 403 quando não for dono")
    void createMenuItemWithPhotoShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());

        when(useCase.createWithPhoto(eq(2L), eq(RESTAURANT_ID), any(MenuItem.class), any(MultipartFile.class)))
                .thenThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"));

        mockMvc.perform(multipart("/api/v1/restaurants/{id}/menu-items/with-photo", RESTAURANT_ID)
                        .file(photo)
                        .param("data", jsonBody("Feijoada", "49.90")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createMenuItemWithPhoto deve retornar 404 quando restaurante não for encontrado")
    void createMenuItemWithPhotoShouldReturn404WhenRestaurantNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "image-bytes".getBytes());

        when(useCase.createWithPhoto(eq(USER_ID), eq(RESTAURANT_ID), any(MenuItem.class), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Restaurant not found"));

        mockMvc.perform(multipart("/api/v1/restaurants/{id}/menu-items/with-photo", RESTAURANT_ID)
                        .file(photo)
                        .param("data", jsonBody("Feijoada", "49.90")))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // ENDPOINT 3: POST /api/v1/menu-items/{id}/photo (Upload avulso)
    // =========================================================================

    @Test
    @DisplayName("uploadMenuItemPhoto deve retornar 200 com item atualizado")
    void uploadMenuItemPhotoShouldReturn200() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        MockMultipartFile photo = new MockMultipartFile("photo", "foto.png", "image/png", "bytes".getBytes());
        when(useCase.uploadPhoto(eq(USER_ID), eq(ITEM_ID), any(MultipartFile.class)))
                .thenReturn(itemWithPhoto(FIREBASE_URL));

        mockMvc.perform(multipart("/api/v1/menu-items/{id}/photo", ITEM_ID)
                        .file(photo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.photoPath").value(FIREBASE_URL));
    }

    @Test
    @DisplayName("uploadMenuItemPhoto deve retornar 403 quando não autenticado")
    void uploadMenuItemPhotoShouldDenyWhenNotAuthenticated() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "foto.png", "image/png", "bytes".getBytes());

        mockMvc.perform(multipart("/api/v1/menu-items/{id}/photo", ITEM_ID)
                        .file(photo))
                .andExpect(status().isForbidden());

        verifyNoInteractions(useCase);
    }

    // =========================================================================
    // ENDPOINT 4: DELETE /api/v1/menu-items/{id}/photo (Remover foto)
    // =========================================================================

    @Test
    @DisplayName("deleteMenuItemPhoto deve retornar 200 e item com photoPath nulo")
    void deleteMenuItemPhotoShouldReturn200() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.deletePhoto(USER_ID, ITEM_ID)).thenReturn(itemWithPhoto(null));

        mockMvc.perform(delete("/api/v1/menu-items/{id}/photo", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.photoPath").doesNotExist());
    }

    @Test
    @DisplayName("deleteMenuItemPhoto deve retornar 403 quando não for dono")
    void deleteMenuItemPhotoShouldReturn403WhenNotOwner() throws Exception {
        authenticate(2L, "CLIENTE");
        when(useCase.deletePhoto(2L, ITEM_ID))
                .thenThrow(new AccessDeniedDomainException("Only the restaurant owner can manage its menu"));

        mockMvc.perform(delete("/api/v1/menu-items/{id}/photo", ITEM_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deleteMenuItemPhoto deve retornar 404 quando item não encontrado")
    void deleteMenuItemPhotoShouldReturn404WhenNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.deletePhoto(USER_ID, 999L))
                .thenThrow(new IllegalArgumentException("MenuItem not found"));

        mockMvc.perform(delete("/api/v1/menu-items/{id}/photo", 999L))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // READS: GET
    // =========================================================================

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

    // =========================================================================
    // UPDATE: PUT
    // =========================================================================

    @Test
    void updateShouldReturn200() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.update(eq(USER_ID), eq(ITEM_ID), any(MenuItem.class))).thenReturn(item());

        mockMvc.perform(put("/api/v1/menu-items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
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
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateShouldReturn404WhenNotFound() throws Exception {
        authenticate(USER_ID, "DONO_DE_RESTAURANTE");
        when(useCase.update(eq(USER_ID), eq(ITEM_ID), any(MenuItem.class)))
                .thenThrow(new IllegalArgumentException("MenuItem not found"));

        mockMvc.perform(put("/api/v1/menu-items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody("Feijoada", "49.90")))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // DELETE: DELETE
    // =========================================================================

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
