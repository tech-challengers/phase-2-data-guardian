package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.infrastructure.security.SecurityContextHelper;
import br.com.restaurante.infrastructure.web.dto.MenuItemRequest;
import br.com.restaurante.infrastructure.web.dto.MenuItemResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuItemController implements MenuItemApi {

    private final MenuItemUseCase menuItemUseCase;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/v1/restaurants/{restaurantId}/menu-items
     * <p>
     * Cria um item de menu recebendo os dados via JSON (inclusive com URL de imagem em photoPath).
     */
    @Override
    public ResponseEntity<MenuItemResponse> createMenuItem(Long restaurantId, MenuItemRequest menuItemRequest) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem item = toDomain(restaurantId, menuItemRequest);
        MenuItem saved = menuItemUseCase.create(userId, restaurantId, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    /**
     * POST /api/v1/restaurants/{restaurantId}/menu-items/with-photo
     * <p>
     * Cria um item de menu recebendo os dados em JSON (campo "data") e o arquivo de imagem (campo "photo")
     * na mesma requisição multipart/form-data. A imagem é salva no Firebase Storage e a URL gerada
     * é associada automaticamente ao item.
     */
    @Override
    public ResponseEntity<MenuItemResponse> createMenuItemWithPhoto(Long restaurantId, String data, MultipartFile photo) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItemRequest request = parseJsonData(data);
        MenuItem item = toDomain(restaurantId, request);
        MenuItem saved = menuItemUseCase.createWithPhoto(userId, restaurantId, item, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @Override
    public ResponseEntity<List<MenuItemResponse>> listMenuItemsByRestaurant(Long restaurantId) {
        List<MenuItemResponse> body = menuItemUseCase.findByRestaurantId(restaurantId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<MenuItemResponse> getMenuItemById(Long id) {
        return ResponseEntity.ok(toResponse(menuItemUseCase.findById(id)));
    }

    /**
     * PUT /api/v1/menu-items/{id}
     * <p>
     * Atualiza um item de menu via JSON. Se possuía foto no Supabase e uma nova URL for informada,
     * a foto anterior do storage é removida automaticamente.
     */
    @Override
    public ResponseEntity<MenuItemResponse> updateMenuItem(Long id, MenuItemRequest menuItemRequest) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem data = toDomain(null, menuItemRequest);
        return ResponseEntity.ok(toResponse(menuItemUseCase.update(userId, id, data)));
    }

    @Override
    public ResponseEntity<Void> deleteMenuItem(Long id) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        menuItemUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/menu-items/{id}/photo
     * <p>
     * Faz upload ou substituição avulsa da foto de um item existente via multipart/form-data.
     */
    @Override
    public ResponseEntity<MenuItemResponse> uploadMenuItemPhoto(Long id, MultipartFile photo) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem updated = menuItemUseCase.uploadPhoto(userId, id, photo);
        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * DELETE /api/v1/menu-items/{id}/photo
     * <p>
     * Remove a foto do Firebase Storage e limpa o photoPath no banco.
     */
    @Override
    public ResponseEntity<MenuItemResponse> deleteMenuItemPhoto(Long id) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem updated = menuItemUseCase.deletePhoto(userId, id);
        return ResponseEntity.ok(toResponse(updated));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MenuItemRequest parseJsonData(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("O campo 'data' com o JSON do item é obrigatório");
        }
        try {
            return objectMapper.readValue(data, MenuItemRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Formato JSON inválido no campo 'data': " + e.getMessage(), e);
        }
    }

    private MenuItem toDomain(Long restaurantId, MenuItemRequest request) {
        return new MenuItem(
                null,
                restaurantId,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                Boolean.TRUE.equals(request.getDineInOnly()),
                request.getPhotoPath()
        );
    }

    private MenuItemResponse toResponse(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        response.setRestaurantId(item.getRestaurantId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setDineInOnly(item.isDineInOnly());
        response.setPhotoPath(item.getPhotoPath());
        return response;
    }
}
