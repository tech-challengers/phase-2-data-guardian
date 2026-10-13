package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.out.RestauranteRepository;
import br.com.restaurante.core.domain.Restaurante;
import br.com.restaurante.core.services.RestaurantService;
import br.com.restaurante.infrastructure.web.dto.mapper.RestauranteConverter;
import br.com.restaurante.infrastructure.web.handlers.RestauranteExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestauranteControllerTest {

    private RestauranteRepository repository;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        repository = mock(RestauranteRepository.class);
        RestaurantService service = new RestaurantService(repository, Mappers.getMapper(RestauranteConverter.class));
        mockMvc = MockMvcBuilders.standaloneSetup(new RestauranteController(service))
                .setControllerAdvice(new RestauranteExceptionHandler())
                .build();
    }

    @Test
    void deveRetornarRestauranteCriadoComIdGerado() throws Exception {
        when(repository.salvar(any(Restaurante.class))).thenAnswer(invocation -> {
            Restaurante restaurante = invocation.getArgument(0);
            assertEquals("Restaurante da Praça", restaurante.getNome());
            assertEquals("Rua das Flores, 100", restaurante.getEndereco());
            assertEquals("Brasileira", restaurante.getGastronomia());
            assertEquals("11:00 às 22:00", restaurante.getHorarioFuncionamento());
            assertEquals(1L, restaurante.getDonoId());
            restaurante.setId(10L);
            return restaurante;
        });

        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Restaurante da Praça", "1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Restaurante da Praça"));

        verify(repository).salvar(any(Restaurante.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "0", "-1"})
    void deveRejeitarDonoIdInvalidoSemSalvar(String donoId) throws Exception {
        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Restaurante da Praça", donoId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarNomeComEspacosSemSalvar() throws Exception {
        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("   ", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Nome é obrigatório"));

        verifyNoInteractions(repository);
    }

    private String request(String nome, String donoId) {
        return """
                {
                  "nome": "%s",
                  "endereco": "Rua das Flores, 100",
                  "gastronomia": "Brasileira",
                  "horarioFuncionamento": "11:00 às 22:00",
                  "donoId": %s
                }
                """.formatted(nome, donoId);
    }
}
