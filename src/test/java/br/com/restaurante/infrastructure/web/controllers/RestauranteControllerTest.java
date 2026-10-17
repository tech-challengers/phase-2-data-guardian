
package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.out.RestauranteRepositoryPort;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.services.RestaurantService;
import br.com.restaurante.infrastructure.web.dto.mapper.RestauranteConverter;
import br.com.restaurante.infrastructure.web.handlers.RestauranteExceptionHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.ServletException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestauranteControllerTest {

    private RestauranteRepositoryPort repository;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();

        repository = mock(RestauranteRepositoryPort.class);

        RestaurantService service = new RestaurantService(
                repository,
                Mappers.getMapper(RestauranteConverter.class)
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(new RestauranteController(service))
                .setControllerAdvice(new RestauranteExceptionHandler())
                .build();
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarUsuario(Long id, String role) {

        AuthenticatedUser usuario = new AuthenticatedUser(
                id,
                "usuario@restaurante.com",
                "Usuario Teste",
                role
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @Test
    void deveRetornarRestauranteCriadoComIdGerado() throws Exception {

        autenticarUsuario(1L, "DONO_DE_RESTAURANTE");

        when(repository.save(any(Restaurante.class)))
                .thenAnswer(invocation -> {

                    Restaurante restaurante = invocation.getArgument(0);

                    assertEquals(
                            "Restaurante da Praça",
                            restaurante.getNome()
                    );

                    assertEquals(
                            "Rua das Flores, 100",
                            restaurante.getEndereco()
                    );

                    assertEquals(
                            "Brasileira",
                            restaurante.getGastronomia()
                    );

                    assertEquals(
                            "11:00 às 22:00",
                            restaurante.getHorarioFuncionamento()
                    );

                    assertEquals(
                            1L,
                            restaurante.getDonoId()
                    );

                    restaurante.setId(10L);

                    return restaurante;
                });

        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Restaurante da Praça")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome")
                        .value("Restaurante da Praça"));

        verify(repository).save(any(Restaurante.class));
    }

    @Test
    void deveImpedirClienteDeCriarRestaurante() throws Exception {

        autenticarUsuario(2L, "CLIENTE");

        Exception exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Restaurante da Praça")))
        );

        assertInstanceOf(
                AccessDeniedException.class,
                exception.getCause()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarNomeComEspacosSemSalvar() throws Exception {

        autenticarUsuario(1L, "DONO_DE_RESTAURANTE");

        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Nome is mandatory"));

        verifyNoInteractions(repository);
    }

    private String request(String nome) {

        return """
                {
                  "nome": "%s",
                  "endereco": "Rua das Flores, 100",
                  "gastronomia": "Brasileira",
                  "horarioFuncionamento": "11:00 às 22:00"
                }
                """.formatted(nome);
    }
}
