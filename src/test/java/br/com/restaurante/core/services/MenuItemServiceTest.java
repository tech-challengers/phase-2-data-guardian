package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.out.MenuItemRepositoryPort;
import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.application.ports.out.StoragePort;
import br.com.restaurante.core.domain.MenuItem;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long ITEM_ID = 100L;
    private static final String SUPABASE_URL = "https://xyz.supabase.co/storage/v1/object/public/menu-items/menu-items/100/photo.jpg";

    @Mock
    private MenuItemRepositoryPort menuItemRepositoryPort;

    @Mock
    private RestauranteRepositoryPort restauranteRepositoryPort;

    @Mock
    private StoragePort storagePort;

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
                new BigDecimal("59.00"), true, "https://url-externa.com/foto.jpg");
    }

    // =========================================================================
    // ENDPOINT 1: CREATE COM URL
    // =========================================================================

    @Test
    @DisplayName("create com URL deve salvar item com sucesso quando usuário for dono")
    void createWithUrlShouldSaveWhenUserIsOwner() {
        MenuItem item = newItem();
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(item)).thenReturn(item);

        MenuItem result = service.create(OWNER_ID, RESTAURANT_ID, item);

        assertSame(item, result);
        assertEquals("https://url-externa.com/foto.jpg", result.getPhotoPath());
        verify(menuItemRepositoryPort).save(item);
        verifyNoInteractions(storagePort);
    }

    @Test
    @DisplayName("create com URL deve lançar AccessDeniedDomainException quando usuário não for dono")
    void createWithUrlShouldDenyWhenUserIsNotOwner() {
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.create(OTHER_USER_ID, RESTAURANT_ID, newItem()));

        verify(menuItemRepositoryPort, never()).save(any());
        verifyNoInteractions(storagePort);
    }

    @Test
    @DisplayName("create com URL deve falhar quando restaurante não for encontrado")
    void createWithUrlShouldFailWhenRestaurantNotFound() {
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(OWNER_ID, RESTAURANT_ID, newItem()));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(menuItemRepositoryPort, never()).save(any());
    }

    // =========================================================================
    // ENDPOINT 2: CREATE COM PHOTO MULTIPART (Upload direto no Supabase)
    // =========================================================================

    @Test
    @DisplayName("createWithPhoto com foto válida deve salvar no storage e atualizar photoPath")
    void createWithPhotoShouldUploadToSupabaseAndSave() {
        MenuItem item = new MenuItem(null, RESTAURANT_ID, "Moqueca", "Capixaba", new BigDecimal("59.00"), true, null);
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.png", "image/png", "fake-img".getBytes());
        MenuItem savedInitial = new MenuItem(ITEM_ID, RESTAURANT_ID, item.getName(), item.getDescription(), item.getPrice(), item.isDineInOnly(), null);

        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(item)).thenReturn(savedInitial);
        when(storagePort.upload(any(InputStream.class), eq("image/png"), eq("menu-items/100/photo.png")))
                .thenReturn(SUPABASE_URL);
        when(menuItemRepositoryPort.save(savedInitial)).thenReturn(savedInitial);

        MenuItem result = service.createWithPhoto(OWNER_ID, RESTAURANT_ID, item, photo);

        assertNotNull(result);
        assertEquals(SUPABASE_URL, result.getPhotoPath());
        verify(storagePort).upload(any(InputStream.class), eq("image/png"), eq("menu-items/100/photo.png"));
        verify(menuItemRepositoryPort, times(2)).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("createWithPhoto com foto nula ou vazia não deve chamar upload no storage")
    void createWithPhotoNullOrEmptyShouldNotCallStorage() {
        MenuItem item = new MenuItem(null, RESTAURANT_ID, "Moqueca", "Capixaba", new BigDecimal("59.00"), true, null);
        MenuItem savedInitial = new MenuItem(ITEM_ID, RESTAURANT_ID, item.getName(), item.getDescription(), item.getPrice(), item.isDineInOnly(), null);

        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(item)).thenReturn(savedInitial);

        MenuItem resultNull = service.createWithPhoto(OWNER_ID, RESTAURANT_ID, item, null);
        assertNotNull(resultNull);
        assertNull(resultNull.getPhotoPath());

        MockMultipartFile emptyPhoto = new MockMultipartFile("photo", "empty.jpg", "image/jpeg", new byte[0]);
        MenuItem resultEmpty = service.createWithPhoto(OWNER_ID, RESTAURANT_ID, item, emptyPhoto);
        assertNotNull(resultEmpty);

        verifyNoInteractions(storagePort);
    }

    @Test
    @DisplayName("createWithPhoto com foto que dispara IOException deve lançar IllegalStateException")
    void createWithPhotoIOExceptionShouldThrowIllegalStateException() throws Exception {
        MenuItem item = new MenuItem(null, RESTAURANT_ID, "Moqueca", "Capixaba", new BigDecimal("59.00"), true, null);
        MultipartFile corruptPhoto = mock(MultipartFile.class);
        when(corruptPhoto.isEmpty()).thenReturn(false);
        when(corruptPhoto.getOriginalFilename()).thenReturn("photo.jpg");
        when(corruptPhoto.getContentType()).thenReturn("image/jpeg");
        when(corruptPhoto.getInputStream()).thenThrow(new IOException("Simulated disk error"));

        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(item)).thenReturn(new MenuItem(ITEM_ID, RESTAURANT_ID, item.getName(), item.getDescription(), item.getPrice(), item.isDineInOnly(), null));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.createWithPhoto(OWNER_ID, RESTAURANT_ID, item, corruptPhoto));

        assertTrue(ex.getMessage().contains("Não foi possível ler o arquivo de imagem"));
    }

    @Test
    @DisplayName("createWithPhoto deve lançar AccessDeniedDomainException quando não for dono")
    void createWithPhotoShouldDenyWhenNotOwner() {
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.png", "image/png", "fake".getBytes());

        assertThrows(AccessDeniedDomainException.class,
                () -> service.createWithPhoto(OTHER_USER_ID, RESTAURANT_ID, newItem(), photo));

        verify(menuItemRepositoryPort, never()).save(any());
        verifyNoInteractions(storagePort);
    }

    // =========================================================================
    // READS
    // =========================================================================

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

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    @DisplayName("update alterando URL e foto anterior era do storage deve excluir a antiga")
    void updateReplacingStoragePhotoUrlShouldDeleteOldFromStorage() {
        MenuItem itemWithPhoto = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc",
                new BigDecimal("49.90"), false, SUPABASE_URL);

        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(itemWithPhoto));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl(SUPABASE_URL)).thenReturn(true);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem data = new MenuItem(null, RESTAURANT_ID, "Feijoada Nova", "Desc", new BigDecimal("55.00"), false, "https://nova-url.com/foto.jpg");
        MenuItem result = service.update(OWNER_ID, ITEM_ID, data);

        verify(storagePort).delete(SUPABASE_URL);
        assertEquals("https://nova-url.com/foto.jpg", result.getPhotoPath());
        assertEquals("Feijoada Nova", result.getName());
    }

    @Test
    @DisplayName("update mantendo a mesma foto não deve chamar delete no storage")
    void updateKeepingSamePhotoShouldNotDeleteFromStorage() {
        MenuItem itemWithPhoto = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc",
                new BigDecimal("49.90"), false, SUPABASE_URL);

        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(itemWithPhoto));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem data = new MenuItem(null, RESTAURANT_ID, "Feijoada", "Nova Desc", new BigDecimal("49.90"), false, SUPABASE_URL);
        MenuItem result = service.update(OWNER_ID, ITEM_ID, data);

        verify(storagePort, never()).delete(anyString());
        assertEquals(SUPABASE_URL, result.getPhotoPath());
    }

    @Test
    @DisplayName("update quando foto anterior era externa não deve chamar delete no storage")
    void updateWithNonStorageOldPhotoShouldNotCallStorageDelete() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing)); // "/img/f.jpg"
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl("/img/f.jpg")).thenReturn(false);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem data = new MenuItem(null, RESTAURANT_ID, "Moqueca", "Capixaba", new BigDecimal("59.00"), true, "https://nova-url.com/m.jpg");
        MenuItem result = service.update(OWNER_ID, ITEM_ID, data);

        verify(storagePort, never()).delete(anyString());
        assertEquals("https://nova-url.com/m.jpg", result.getPhotoPath());
    }

    @Test
    @DisplayName("update quando ambos oldPhoto e newPhoto são nulos não deve lançar exceção")
    void updateWithNullOldAndNewPhoto() {
        MenuItem itemNoPhoto = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, null);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(itemNoPhoto));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem data = new MenuItem(null, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, null);
        MenuItem result = service.update(OWNER_ID, ITEM_ID, data);

        assertNull(result.getPhotoPath());
        verify(storagePort, never()).delete(anyString());
    }

    @Test
    void updateShouldDenyWhenUserIsNotOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.update(OTHER_USER_ID, ITEM_ID, newItem()));

        verify(menuItemRepositoryPort, never()).save(any());
        verifyNoInteractions(storagePort);
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

    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    @DisplayName("delete deve remover foto do storage se pertencer ao storage e depois remover item")
    void deleteShouldRemovePhotoFromStorageWhenStorageUrl() {
        MenuItem itemWithPhoto = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc",
                new BigDecimal("49.90"), false, SUPABASE_URL);

        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(itemWithPhoto));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl(SUPABASE_URL)).thenReturn(true);

        service.delete(OWNER_ID, ITEM_ID);

        verify(storagePort).delete(SUPABASE_URL);
        verify(menuItemRepositoryPort).deleteById(ITEM_ID);
    }

    @Test
    @DisplayName("delete não deve chamar storage delete se foto for externa")
    void deleteShouldNotCallStorageWhenPhotoIsNotStorage() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl("/img/f.jpg")).thenReturn(false);

        service.delete(OWNER_ID, ITEM_ID);

        verify(storagePort, never()).delete(anyString());
        verify(menuItemRepositoryPort).deleteById(ITEM_ID);
    }

    @Test
    @DisplayName("delete com item com foto nula não deve chamar storage delete")
    void deleteWithNullPhotoShouldNotCallStorage() {
        MenuItem itemNoPhoto = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, null);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(itemNoPhoto));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        service.delete(OWNER_ID, ITEM_ID);

        verify(storagePort, never()).delete(anyString());
        verify(menuItemRepositoryPort).deleteById(ITEM_ID);
    }

    @Test
    void deleteShouldDenyWhenUserIsNotOwner() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        assertThrows(AccessDeniedDomainException.class,
                () -> service.delete(OTHER_USER_ID, ITEM_ID));

        verify(menuItemRepositoryPort, never()).deleteById(any());
        verifyNoInteractions(storagePort);
    }

    @Test
    void deleteShouldFailWhenItemNotFound() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.delete(OWNER_ID, ITEM_ID));

        verify(menuItemRepositoryPort, never()).deleteById(any());
    }

    // =========================================================================
    // UPLOAD PHOTO (avulso)
    // =========================================================================

    @Test
    @DisplayName("uploadPhoto com foto válida deve remover foto anterior do storage e fazer upload da nova")
    void uploadPhotoShouldReplaceExistingStoragePhoto() {
        MenuItem item = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, SUPABASE_URL);
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.gif", "image/gif", "gif-data".getBytes());
        String newUrl = "https://xyz.supabase.co/storage/v1/object/public/menu-items/menu-items/100/photo.gif";

        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl(SUPABASE_URL)).thenReturn(true);
        when(storagePort.upload(any(InputStream.class), eq("image/gif"), eq("menu-items/100/photo.gif"))).thenReturn(newUrl);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.uploadPhoto(OWNER_ID, ITEM_ID, photo);

        verify(storagePort).delete(SUPABASE_URL);
        verify(storagePort).upload(any(InputStream.class), eq("image/gif"), eq("menu-items/100/photo.gif"));
        assertEquals(newUrl, result.getPhotoPath());
    }

    @Test
    @DisplayName("uploadPhoto em item sem foto anterior deve apenas fazer upload da nova")
    void uploadPhotoOnItemWithoutPreviousPhoto() {
        MenuItem item = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, null);
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.png", "image/png", "png-data".getBytes());
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.upload(any(InputStream.class), eq("image/png"), eq("menu-items/100/photo.png"))).thenReturn(SUPABASE_URL);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.uploadPhoto(OWNER_ID, ITEM_ID, photo);

        verify(storagePort, never()).delete(anyString());
        assertEquals(SUPABASE_URL, result.getPhotoPath());
    }

    @Test
    @DisplayName("uploadPhoto com foto nula deve lançar IllegalArgumentException")
    void uploadPhotoWithNullFileShouldThrowIllegalArgumentException() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadPhoto(OWNER_ID, ITEM_ID, null));

        assertEquals("O arquivo de imagem é obrigatório", ex.getMessage());
    }

    @Test
    @DisplayName("uploadPhoto com foto vazia deve lançar IllegalArgumentException")
    void uploadPhotoWithEmptyFileShouldThrowIllegalArgumentException() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        MockMultipartFile emptyPhoto = new MockMultipartFile("photo", "empty.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadPhoto(OWNER_ID, ITEM_ID, emptyPhoto));

        assertEquals("O arquivo de imagem é obrigatório", ex.getMessage());
    }

    // =========================================================================
    // DELETE PHOTO
    // =========================================================================

    @Test
    @DisplayName("deletePhoto com foto do storage deve excluir do storage e definir photoPath como null")
    void deletePhotoWithStoragePhotoShouldDeleteFromStorageAndNullifyPath() {
        MenuItem item = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, SUPABASE_URL);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl(SUPABASE_URL)).thenReturn(true);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.deletePhoto(OWNER_ID, ITEM_ID);

        verify(storagePort).delete(SUPABASE_URL);
        assertNull(result.getPhotoPath());
    }

    @Test
    @DisplayName("deletePhoto com foto externa não deve chamar storage")
    void deletePhotoWithNonStoragePhotoShouldNotCallStorage() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(storagePort.isStorageUrl("/img/f.jpg")).thenReturn(false);
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.deletePhoto(OWNER_ID, ITEM_ID);

        verify(storagePort, never()).delete(anyString());
        assertNull(result.getPhotoPath());
    }

    @Test
    @DisplayName("deletePhoto com photoPath já nulo não deve chamar storage")
    void deletePhotoWithAlreadyNullPhoto() {
        MenuItem item = new MenuItem(ITEM_ID, RESTAURANT_ID, "Feijoada", "Desc", new BigDecimal("49.90"), false, null);
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = service.deletePhoto(OWNER_ID, ITEM_ID);

        verify(storagePort, never()).delete(anyString());
        assertNull(result.getPhotoPath());
    }

    // =========================================================================
    // EXTENSIONS & HELPER EDGE CASES
    // =========================================================================

    @Test
    @DisplayName("resolveExtension deve mapear corretamente extensões a partir do mime type ou nome de arquivo")
    void shouldResolveExtensionsCorrectly() {
        when(menuItemRepositoryPort.findById(ITEM_ID)).thenReturn(Optional.of(existing));
        when(restauranteRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurante));
        when(menuItemRepositoryPort.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        // Sem extensão no nome, mime image/png -> .png
        MockMultipartFile pngFile = new MockMultipartFile("photo", "noext", "image/png", "a".getBytes());
        service.uploadPhoto(OWNER_ID, ITEM_ID, pngFile);
        verify(storagePort).upload(any(InputStream.class), eq("image/png"), eq("menu-items/100/photo.png"));

        // Sem extensão no nome, mime image/gif -> .gif
        MockMultipartFile gifFile = new MockMultipartFile("photo", "noext", "image/gif", "a".getBytes());
        service.uploadPhoto(OWNER_ID, ITEM_ID, gifFile);
        verify(storagePort).upload(any(InputStream.class), eq("image/gif"), eq("menu-items/100/photo.gif"));

        // Sem extensão no nome, mime image/webp -> .webp
        MockMultipartFile webpFile = new MockMultipartFile("photo", "noext", "image/webp", "a".getBytes());
        service.uploadPhoto(OWNER_ID, ITEM_ID, webpFile);
        verify(storagePort).upload(any(InputStream.class), eq("image/webp"), eq("menu-items/100/photo.webp"));

        // Sem extensão e mime desconhecido -> fallback .jpg
        MockMultipartFile unknownFile = new MockMultipartFile("photo", "noext", "application/octet-stream", "a".getBytes());
        service.uploadPhoto(OWNER_ID, ITEM_ID, unknownFile);
        verify(storagePort).upload(any(InputStream.class), eq("application/octet-stream"), eq("menu-items/100/photo.jpg"));

        // Nome nulo e mime nulo -> fallback .jpg
        MockMultipartFile nullMimeFile = new MockMultipartFile("photo", null, null, "a".getBytes());
        service.uploadPhoto(OWNER_ID, ITEM_ID, nullMimeFile);
        verify(storagePort).upload(any(InputStream.class), isNull(), eq("menu-items/100/photo.jpg"));
    }
}
