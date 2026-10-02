package br.com.dataguardian.restaurante.core.services;

import br.com.dataguardian.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.dataguardian.restaurante.core.domain.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTypeServiceTest {

    private UserTypeRepositoryPort repositoryPort;
    private UserTypeService service;

    @BeforeEach
    void setUp() {
        repositoryPort = mock(UserTypeRepositoryPort.class);
        service = new UserTypeService(repositoryPort);
    }

    @Test
    void create_validUserType_success() {
        UserType ut = new UserType(null, "Cliente");
        when(repositoryPort.existsByName("Cliente")).thenReturn(false);
        when(repositoryPort.save(ut)).thenReturn(new UserType(1L, "Cliente"));

        UserType created = service.create(ut);
        assertNotNull(created.getId());
        assertEquals("Cliente", created.getName());
    }

    @Test
    void create_nullOrEmptyName_throwsException() {
        UserType empty = new UserType();
        assertThrows(IllegalArgumentException.class, () -> service.create(empty));

        UserType blank = new UserType();
        try {
            blank.setName("   ");
        } catch (IllegalArgumentException ignored) {}
        assertThrows(IllegalArgumentException.class, () -> service.create(new UserType()));
    }

    @Test
    void create_existingName_throwsException() {
        UserType ut = new UserType(null, "Cliente");
        when(repositoryPort.existsByName("Cliente")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(ut));
    }

    @Test
    void findById_success() {
        UserType ut = new UserType(1L, "Cliente");
        when(repositoryPort.findById(1L)).thenReturn(Optional.of(ut));
        
        Optional<UserType> result = service.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Cliente", result.get().getName());
    }

    @Test
    void findAll_success() {
        when(repositoryPort.findAll()).thenReturn(List.of(new UserType(1L, "Cliente")));
        List<UserType> list = service.findAll();
        assertEquals(1, list.size());
    }

    @Test
    void update_validId_success() {
        UserType existing = new UserType(1L, "Old");
        when(repositoryPort.findById(1L)).thenReturn(Optional.of(existing));
        when(repositoryPort.save(existing)).thenReturn(new UserType(1L, "New"));

        UserType updated = service.update(1L, new UserType(null, "New"));
        assertEquals("New", updated.getName());
    }

    @Test
    void update_emptyName_throwsException() {
        UserType empty = new UserType();
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, empty));
    }

    @Test
    void update_notFound_throwsException() {
        when(repositoryPort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, new UserType(null, "New")));
    }

    @Test
    void delete_existing_success() {
        when(repositoryPort.findById(1L)).thenReturn(Optional.of(new UserType(1L, "X")));
        assertDoesNotThrow(() -> service.delete(1L));
        verify(repositoryPort, times(1)).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(repositoryPort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.delete(1L));
    }
}
