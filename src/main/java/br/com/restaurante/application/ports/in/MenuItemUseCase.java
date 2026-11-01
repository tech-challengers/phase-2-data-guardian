package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.MenuItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MenuItemUseCase {

    /**
     * Cria um item de menu informando a URL externa da foto (ou sem foto).
     */
    MenuItem create(Long userId, Long restaurantId, MenuItem item);

    /**
     * Cria um item de menu recebendo o arquivo de imagem, persistindo-o no Firebase Storage.
     */
    MenuItem createWithPhoto(Long userId, Long restaurantId, MenuItem item, MultipartFile photo);

    List<MenuItem> findByRestaurantId(Long restaurantId);

    MenuItem findById(Long id);

    /**
     * Atualiza os dados do item. Se o item tinha foto no Firebase e agora possui outra URL, remove a anterior.
     */
    MenuItem update(Long userId, Long id, MenuItem data);

    /**
     * Exclui o item e remove a foto do storage caso ela pertença ao Firebase Storage.
     */
    void delete(Long userId, Long id);

    /**
     * Faz upload/substituição avulsa da foto de um item existente.
     */
    MenuItem uploadPhoto(Long userId, Long menuItemId, MultipartFile photo);

    /**
     * Remove a foto do item do storage e define photoPath como null no banco.
     */
    MenuItem deletePhoto(Long userId, Long menuItemId);
}
