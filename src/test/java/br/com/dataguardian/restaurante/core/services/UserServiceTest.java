package br.com.dataguardian.restaurante.core.services;

import br.com.dataguardian.restaurante.application.ports.out.UserRepositoryPort;
import br.com.dataguardian.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.dataguardian.restaurante.core.domain.User;
import br.com.dataguardian.restaurante.core.domain.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepositoryPort userRepositoryPort;
    private UserTypeRepositoryPort userTypeRepositoryPort;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        userTypeRepositoryPort = mock(UserTypeRepositoryPort.class);
        service = new UserService(userRepositoryPort, userTypeRepositoryPort);
    }

    @Test
    void create_validUser_success() {
        User user = new User(null, "John", "john@example.com", null);
        when(userRepositoryPort.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepositoryPort.save(user)).thenReturn(new User(1L, "John", "john@example.com", null));

        User created = service.create(user);
        assertNotNull(created.getId());
        assertEquals("john@example.com", created.getEmail());
    }

    @Test
    void create_existingEmail_throwsException() {
        User user = new User(null, "John", "john@example.com", null);
        when(userRepositoryPort.existsByEmail("john@example.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.create(user));
    }

    @Test
    void findAll_success() {
        User u1 = new User(1L, "John", "john@example.com", null);
        when(userRepositoryPort.findAll()).thenReturn(List.of(u1));

        List<User> users = service.findAll();
        assertEquals(1, users.size());
        assertEquals("John", users.getFirst().getName());
    }

    @Test
    void assignUserType_validInputs_success() {
        User user = new User(1L, "John", "j@e.com", null);
        UserType type = new UserType(1L, "Cliente");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userTypeRepositoryPort.findById(1L)).thenReturn(Optional.of(type));
        when(userRepositoryPort.save(user)).thenReturn(user);

        User result = service.assignUserType(1L, 1L);
        assertNotNull(result.getUserType());
        assertEquals("Cliente", result.getUserType().getName());
    }

    @Test
    void assignUserType_userNotFound_throwsException() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.assignUserType(1L, 1L));
    }

    @Test
    void assignUserType_userTypeNotFound_throwsException() {
        User user = new User(1L, "John", "j@e.com", null);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userTypeRepositoryPort.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.assignUserType(1L, 2L));
    }
}
