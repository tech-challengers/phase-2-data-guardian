package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.infrastructure.security.SecurityContextHelper;
import br.com.restaurante.infrastructure.web.dto.MenuItemRequest;
import br.com.restaurante.infrastructure.web.dto.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MenuItemController implements MenuItemApi {

    private final MenuItemUseCase menuItemUseCase;

    @Override
    public ResponseEntity<MenuItemResponse> createMenuItem(Long restaurantId, MenuItemRequest request) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem item = toDomain(restaurantId, request);
        MenuItem saved = menuItemUseCase.create(userId, restaurantId, item);
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

    @Override
    public ResponseEntity<MenuItemResponse> updateMenuItem(Long id, MenuItemRequest request) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        MenuItem data = toDomain(null, request);
        return ResponseEntity.ok(toResponse(menuItemUseCase.update(userId, id, data)));
    }

    @Override
    public ResponseEntity<Void> deleteMenuItem(Long id) {
        Long userId = SecurityContextHelper.getRequiredUserId();
        menuItemUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private MenuItem toDomain(Long restaurantId, MenuItemRequest request) {
        return new MenuItem(null, restaurantId, request.getName(), request.getDescription(),
                request.getPrice(), Boolean.TRUE.equals(request.getDineInOnly()),
                request.getPhotoPath());
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