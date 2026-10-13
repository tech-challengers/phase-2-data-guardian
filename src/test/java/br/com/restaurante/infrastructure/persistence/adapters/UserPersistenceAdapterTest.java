package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.infrastructure.persistence.entities.UserEntity;
import br.com.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import br.com.restaurante.infrastructure.persistence.repositories.SpringUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserPersistenceAdapterTest {

    private SpringUserRepository repository;
    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringUserRepository.class);
        adapter = new UserPersistenceAdapter(repository);
    }

    @Test
    void save_success() {
        User u = new User(null, "B", "b@b.com", new UserType(1L, "T"));
        UserEntity ue = new UserEntity(1L, "B", "b@b.com", new UserTypeEntity(1L, "T"));
        when(repository.save(any())).thenReturn(ue);

        User saved = adapter.save(u);
        assertEquals(1L, saved.getId());
        assertEquals("T", saved.getUserType().getName());
    }

    @Test
    void save_noUserType_success() {
        User u = new User(null, "B", "b@b.com", null);
        UserEntity ue = new UserEntity(1L, "B", "b@b.com", null);
        when(repository.save(any())).thenReturn(ue);

        User saved = adapter.save(u);
        assertNull(saved.getUserType());
    }

    @Test
    void findById_success() {
        UserEntity ue = new UserEntity(1L, "B", "b@b.com", null);
        when(repository.findById(1L)).thenReturn(Optional.of(ue));
        assertTrue(adapter.findById(1L).isPresent());
    }

    @Test
    void findAll_success() {
        when(repository.findAll()).thenReturn(List.of(new UserEntity(1L, "B", "b@b.com", null)));
        assertEquals(1, adapter.findAll().size());
    }

    @Test
    void existsByEmail_success() {
        when(repository.existsByEmail("A")).thenReturn(true);
        assertTrue(adapter.existsByEmail("A"));
    }

    @Test
    void findByEmail_success() {
        when(repository.findByEmail("A")).thenReturn(Optional.of(new UserEntity(1L, "B", "A", null)));
        assertTrue(adapter.findByEmail("A").isPresent());
    }
}
