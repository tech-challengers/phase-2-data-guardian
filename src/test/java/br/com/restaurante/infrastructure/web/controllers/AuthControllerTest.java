package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.core.domain.exceptions.InvalidCredentialsException;
import br.com.restaurante.infrastructure.web.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthUseCase authUseCase;
    private AuthController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authUseCase = mock(AuthUseCase.class);
        controller = new AuthController(authUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("dono@restaurante.com");
        req.setPassword("admin123");

        User user = new User(1L, "Dono", "dono@restaurante.com", "pass",
                new UserType(1L, UserType.DONO_DE_RESTAURANTE));
        AuthUseCase.LoginResult result = new AuthUseCase.LoginResult("jwt-token-123", "Bearer", 86400000L, user);

        when(authUseCase.login("dono@restaurante.com", "admin123")).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.email").value("dono@restaurante.com"))
                .andExpect(jsonPath("$.user.userType.name").value(UserType.DONO_DE_RESTAURANTE));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("dono@restaurante.com");
        req.setPassword("wrongpass");

        when(authUseCase.login("dono@restaurante.com", "wrongpass"))
                .thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Credenciais inválidas"));
    }
}
