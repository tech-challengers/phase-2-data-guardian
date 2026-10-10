package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import br.com.restaurante.infrastructure.persistence.repositories.SpringUserTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserTypePersistenceAdapterTest {

    private SpringUserTypeRepository repository;
    private UserTypePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(SpringUserTypeRepository.class);
        adapter = new UserTypePersistenceAdapter(repository);
    }

    @Test
    void save_success() {
        UserType ut = new UserType(null, "A");
        UserTypeEntity entity = new UserTypeEntity(1L, "A");
        when(repository.save(any(UserTypeEntity.class))).thenReturn(entity);

        UserType saved = adapter.save(ut);
        assertEquals(1L, saved.getId());
        assertEquals("A", saved.getName());
    }

    @Test
    void findById_success() {
        UserTypeEntity entity = new UserTypeEntity(1L, "A");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<UserType> ut = adapter.findById(1L);
        assertTrue(ut.isPresent());
        assertEquals("A", ut.get().getName());
    }

    @Test
    void findByName_success() {
        UserTypeEntity entity = new UserTypeEntity(1L, "DONO_DE_RESTAURANTE");
        when(repository.findByName("DONO_DE_RESTAURANTE")).thenReturn(Optional.of(entity));

        Optional<UserType> ut = adapter.findByName("DONO_DE_RESTAURANTE");
        assertTrue(ut.isPresent());
        assertEquals("DONO_DE_RESTAURANTE", ut.get().getName());
    }

    @Test
    void findAll_success() {
        when(repository.findAll()).thenReturn(List.of(new UserTypeEntity(1L, "A")));
        List<UserType> res = adapter.findAll();
        assertEquals(1, res.size());
    }

    @Test
    void deleteById_success() {
        adapter.deleteById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void existsByName_success() {
        when(repository.existsByName("A")).thenReturn(true);
        assertTrue(adapter.existsByName("A"));
    }
}
