package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.core.domain.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepositoryPort userRepositoryPort;
    private PasswordEncoderPort passwordEncoderPort;
    private TokenPort tokenPort;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        tokenPort = mock(TokenPort.class);
        authService = new AuthService(userRepositoryPort, passwordEncoderPort, tokenPort, 86400000L);
    }

    @Test
    void login_validCredentials_returnsTokenAndUserInfo() {
        UserType donoType = new UserType(1L, UserType.DONO_DE_RESTAURANTE);
        User user = new User(1L, "Dono", "dono@restaurante.com", "encoded-pass", donoType);

        when(userRepositoryPort.findByEmail("dono@restaurante.com")).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("admin123", "encoded-pass")).thenReturn(true);
        when(tokenPort.generateToken(user)).thenReturn("mocked.jwt.token");

        AuthUseCase.LoginResult result = authService.login("dono@restaurante.com", "admin123");

        assertNotNull(result);
        assertEquals("mocked.jwt.token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(86400000L, result.expiresIn());
        assertEquals("dono@restaurante.com", result.user().getEmail());
        assertEquals(UserType.DONO_DE_RESTAURANTE, result.user().getUserType().getName());

        verify(tokenPort, times(1)).generateToken(user);
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(userRepositoryPort.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login("unknown@mail.com", "password"));
    }

    @Test
    void login_incorrectPassword_throwsInvalidCredentials() {
        UserType clienteType = new UserType(2L, UserType.CLIENTE);
        User user = new User(2L, "Cliente", "cliente@email.com", "encoded-pass", clienteType);

        when(userRepositoryPort.findByEmail("cliente@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("wrongpass", "encoded-pass")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login("cliente@email.com", "wrongpass"));
    }

    @Test
    void login_userWithoutUserType_throwsInvalidCredentials() {
        User user = new User(3L, "Sem Tipo", "user@test.com", "encoded-pass", null);

        when(userRepositoryPort.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("pass", "encoded-pass")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () ->
                authService.login("user@test.com", "pass"));
    }

    @Test
    void login_emptyEmailOrPassword_throwsInvalidCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> authService.login("", "pass"));
        assertThrows(InvalidCredentialsException.class, () -> authService.login("  ", "pass"));
        assertThrows(InvalidCredentialsException.class, () -> authService.login(null, "pass"));
        assertThrows(InvalidCredentialsException.class, () -> authService.login("test@test.com", ""));
        assertThrows(InvalidCredentialsException.class, () -> authService.login("test@test.com", null));
    }
}
