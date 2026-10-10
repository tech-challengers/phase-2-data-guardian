package br.com.restaurante.infrastructure.persistence;

import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    private UserTypeRepositoryPort userTypeRepositoryPort;
    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        userTypeRepositoryPort = mock(UserTypeRepositoryPort.class);
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);

        dataInitializer = new DataInitializer(
                userTypeRepositoryPort,
                userRepositoryPort,
                passwordEncoderPort
        );
    }

    @Test
    void run_emptyDatabase_seedsUserTypesAndUsers() {
        // Nothing exists in database yet
        when(userTypeRepositoryPort.findByName(UserType.DONO_DE_RESTAURANTE)).thenReturn(Optional.empty());
        when(userTypeRepositoryPort.findByName(UserType.CLIENTE)).thenReturn(Optional.empty());
        when(userRepositoryPort.findByEmail("dono@restaurante.com")).thenReturn(Optional.empty());
        when(userRepositoryPort.findByEmail("cliente@email.com")).thenReturn(Optional.empty());

        UserType savedDonoType = new UserType(1L, UserType.DONO_DE_RESTAURANTE);
        UserType savedClienteType = new UserType(2L, UserType.CLIENTE);
        User savedDonoUser = new User(1L, "Dono do Restaurante", "dono@restaurante.com", "encoded-admin123", savedDonoType);
        User savedClienteUser = new User(2L, "Cliente Teste", "cliente@email.com", "encoded-cliente123", savedClienteType);

        when(userTypeRepositoryPort.save(argThat(ut -> ut != null && UserType.DONO_DE_RESTAURANTE.equals(ut.getName())))).thenReturn(savedDonoType);
        when(userTypeRepositoryPort.save(argThat(ut -> ut != null && UserType.CLIENTE.equals(ut.getName())))).thenReturn(savedClienteType);
        when(passwordEncoderPort.encode("admin123")).thenReturn("encoded-admin123");
        when(passwordEncoderPort.encode("cliente123")).thenReturn("encoded-cliente123");
        when(userRepositoryPort.save(argThat(u -> u != null && "dono@restaurante.com".equals(u.getEmail())))).thenReturn(savedDonoUser);
        when(userRepositoryPort.save(argThat(u -> u != null && "cliente@email.com".equals(u.getEmail())))).thenReturn(savedClienteUser);

        dataInitializer.run();

        // Verify user types were saved
        verify(userTypeRepositoryPort, times(2)).save(any(UserType.class));

        // Verify users were saved with encoded passwords
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort, times(2)).save(userCaptor.capture());
        assertTrue(userCaptor.getAllValues().stream().anyMatch(u -> "dono@restaurante.com".equals(u.getEmail())));
        assertTrue(userCaptor.getAllValues().stream().anyMatch(u -> "cliente@email.com".equals(u.getEmail())));
    }

    @Test
    void run_existingDatabase_doesNotDuplicateData() {
        UserType existingDonoType = new UserType(1L, UserType.DONO_DE_RESTAURANTE);
        UserType existingClienteType = new UserType(2L, UserType.CLIENTE);
        User existingDono = new User(1L, "Dono", "dono@restaurante.com", "hash", existingDonoType);
        User existingCliente = new User(2L, "Cliente", "cliente@email.com", "hash", existingClienteType);

        when(userTypeRepositoryPort.findByName(UserType.DONO_DE_RESTAURANTE)).thenReturn(Optional.of(existingDonoType));
        when(userTypeRepositoryPort.findByName(UserType.CLIENTE)).thenReturn(Optional.of(existingClienteType));
        when(userRepositoryPort.findByEmail("dono@restaurante.com")).thenReturn(Optional.of(existingDono));
        when(userRepositoryPort.findByEmail("cliente@email.com")).thenReturn(Optional.of(existingCliente));

        dataInitializer.run();

        verify(userTypeRepositoryPort, never()).save(any());
        verify(userRepositoryPort, never()).save(any());
    }
}
