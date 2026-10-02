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
    void findById_existingUser_returnsUser() {
        User user = new User(1L, "Alice", "alice@example.com", null);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));

        User found = service.findById(1L);
        assertEquals("Alice", found.getName());
    }

    @Test
    void findById_nonExistingUser_throwsException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.findById(99L));
    }

    @Test
    void update_validUser_sameEmail_success() {
        User existingUser = new User(1L, "Bob", "bob@example.com", null);
        User updatedInfo = new User(null, "Bob Silva", "bob@example.com", null);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.save(existingUser)).thenReturn(existingUser);

        User result = service.update(1L, updatedInfo);
        assertEquals("Bob Silva", result.getName());
        assertEquals("bob@example.com", result.getEmail());
        verify(userRepositoryPort, never()).existsByEmail(anyString());
    }

    @Test
    void update_validUser_newEmail_success() {
        User existingUser = new User(1L, "Charlie", "charlie@example.com", null);
        User updatedInfo = new User(null, "Charlie", "novo@example.com", null);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail("novo@example.com")).thenReturn(false);
        when(userRepositoryPort.save(existingUser)).thenReturn(existingUser);

        User result = service.update(1L, updatedInfo);
        assertEquals("novo@example.com", result.getEmail());
    }

    @Test
    void update_existingNewEmail_throwsException() {
        User existingUser = new User(1L, "Diana", "diana@example.com", null);
        User updatedInfo = new User(null, "Diana", "usado@example.com", null);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail("usado@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.update(1L, updatedInfo));
    }

    @Test
    void delete_existingUser_success() {
        User existingUser = new User(1L, "Eve", "eve@example.com", null);
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(existingUser));

        service.delete(1L);
        verify(userRepositoryPort, times(1)).deleteById(1L);
    }
}