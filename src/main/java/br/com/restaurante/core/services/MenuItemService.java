package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.MenuItemUseCase;
import br.com.restaurante.application.ports.out.MenuItemRepositoryPort;
import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.application.ports.out.StoragePort;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class MenuItemService implements MenuItemUseCase {

    private final MenuItemRepositoryPort menuItemRepositoryPort;
    private final RestauranteRepositoryPort restauranteRepositoryPort;
    private final StoragePort storagePort;

    public MenuItemService(MenuItemRepositoryPort menuItemRepositoryPort,
                           RestauranteRepositoryPort restauranteRepositoryPort,
                           StoragePort storagePort) {
        this.menuItemRepositoryPort = menuItemRepositoryPort;
        this.restauranteRepositoryPort = restauranteRepositoryPort;
        this.storagePort = storagePort;
    }

    @Override
    public MenuItem create(Long userId, Long restaurantId, MenuItem item) {
        checkOwner(userId, restaurantId);
        return menuItemRepositoryPort.save(item);
    }

    @Override
    public MenuItem createWithPhoto(Long userId, Long restaurantId, MenuItem item, MultipartFile photo) {
        checkOwner(userId, restaurantId);

        MenuItem saved = menuItemRepositoryPort.save(item);

        if (photo != null && !photo.isEmpty()) {
            String publicUrl = uploadToStorage(photo, saved.getId());
            saved.update(saved.getName(), saved.getDescription(), saved.getPrice(),
                    saved.isDineInOnly(), publicUrl);
            saved = menuItemRepositoryPort.save(saved);
        }

        return saved;
    }

    @Override
    public List<MenuItem> findByRestaurantId(Long restaurantId) {
        return menuItemRepositoryPort.findByRestaurantId(restaurantId);
    }

    @Override
    public MenuItem findById(Long id) {
        return menuItemRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found"));
    }

    @Override
    public MenuItem update(Long userId, Long id, MenuItem data) {
        MenuItem item = findById(id);
        checkOwner(userId, item.getRestaurantId());

        String oldPhoto = item.getPhotoPath();
        String newPhoto = data.getPhotoPath();

        // Se a foto anterior pertencia ao storage gerenciado e a nova for diferente, remove do storage
        if (oldPhoto != null && !oldPhoto.equals(newPhoto) && storagePort.isStorageUrl(oldPhoto)) {
            storagePort.delete(oldPhoto);
        }

        item.update(data.getName(), data.getDescription(), data.getPrice(),
                data.isDineInOnly(), newPhoto);
        return menuItemRepositoryPort.save(item);
    }

    @Override
    public void delete(Long userId, Long id) {
        MenuItem item = findById(id);
        checkOwner(userId, item.getRestaurantId());

        if (item.getPhotoPath() != null && storagePort.isStorageUrl(item.getPhotoPath())) {
            storagePort.delete(item.getPhotoPath());
        }

        menuItemRepositoryPort.deleteById(id);
    }

    @Override
    public MenuItem uploadPhoto(Long userId, Long menuItemId, MultipartFile photo) {
        MenuItem item = findById(menuItemId);
        checkOwner(userId, item.getRestaurantId());

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de imagem é obrigatório");
        }

        if (item.getPhotoPath() != null && storagePort.isStorageUrl(item.getPhotoPath())) {
            storagePort.delete(item.getPhotoPath());
        }

        String publicUrl = uploadToStorage(photo, menuItemId);
        item.update(item.getName(), item.getDescription(), item.getPrice(),
                item.isDineInOnly(), publicUrl);
        return menuItemRepositoryPort.save(item);
    }

    @Override
    public MenuItem deletePhoto(Long userId, Long menuItemId) {
        MenuItem item = findById(menuItemId);
        checkOwner(userId, item.getRestaurantId());

        if (item.getPhotoPath() != null && storagePort.isStorageUrl(item.getPhotoPath())) {
            storagePort.delete(item.getPhotoPath());
        }

        item.update(item.getName(), item.getDescription(), item.getPrice(),
                item.isDineInOnly(), null);
        return menuItemRepositoryPort.save(item);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private String uploadToStorage(MultipartFile photo, Long itemId) {
        try {
            String extension = resolveExtension(photo.getOriginalFilename(), photo.getContentType());
            String storagePath = "menu-items/" + itemId + "/photo" + extension;
            return storagePort.upload(photo.getInputStream(), photo.getContentType(), storagePath);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo de imagem: " + e.getMessage(), e);
        }
    }

    private void checkOwner(Long userId, Long restaurantId) {
        Restaurante restaurante = restauranteRepositoryPort.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        if (!restaurante.getDonoId().equals(userId)) {
            throw new AccessDeniedDomainException("Only the restaurant owner can manage its menu");
        }
    }

    private String resolveExtension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return switch (contentType != null ? contentType : "") {
            case "image/png"  -> ".png";
            case "image/gif"  -> ".gif";
            case "image/webp" -> ".webp";
            default           -> ".jpg";
        };
    }
}
