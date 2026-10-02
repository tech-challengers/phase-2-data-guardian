package br.com.dataguardian.restaurante.infrastructure.web.controllers;

import br.com.dataguardian.restaurante.application.ports.in.UserTypeUseCase;
import br.com.dataguardian.restaurante.core.domain.UserType;
import br.com.dataguardian.restaurante.infrastructure.web.dto.UserTypeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserTypeControllerTest {

    private UserTypeUseCase useCase;
    private UserTypeController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        useCase = mock(UserTypeUseCase.class);
        controller = new UserTypeController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createUserType_success() throws Exception {
        UserTypeRequest req = new UserTypeRequest();
        req.setName("Cliente");

        when(useCase.create(any())).thenReturn(new UserType(1L, "Cliente"));

        mockMvc.perform(post("/api/v1/user-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cliente"));

        verify(useCase, times(1)).create(any());
    }

    @Test
    void createUserType_emptyName_returnsProblemDetailBadRequest() throws Exception {
        UserTypeRequest req = new UserTypeRequest();
        req.setName("");

        when(useCase.create(any())).thenThrow(new IllegalArgumentException("Name cannot be empty"));

        mockMvc.perform(post("/api/v1/user-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Name cannot be empty"));
    }

    @Test
    void getUserTypeById_success() throws Exception {
        when(useCase.findById(1L)).thenReturn(Optional.of(new UserType(1L, "Cliente")));

        mockMvc.perform(get("/api/v1/user-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cliente"));
    }

    @Test
    void getUserTypeById_notFound_returnsProblemDetailNotFound() throws Exception {
        when(useCase.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user-types/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("UserType not found"));
    }

    @Test
    void listUserTypes_success() throws Exception {
        when(useCase.findAll()).thenReturn(List.of(
                new UserType(1L, "Dono de Restaurante"),
                new UserType(2L, "Cliente")
        ));

        mockMvc.perform(get("/api/v1/user-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dono de Restaurante"))
                .andExpect(jsonPath("$[1].name").value("Cliente"));
    }

    @Test
    void updateUserType_success() throws Exception {
        UserTypeRequest req = new UserTypeRequest();
        req.setName("Cliente VIP");

        when(useCase.update(eq(1L), any())).thenReturn(new UserType(1L, "Cliente VIP"));

        mockMvc.perform(put("/api/v1/user-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Cliente VIP"));
    }

    @Test
    void deleteUserType_success() throws Exception {
        doNothing().when(useCase).delete(1L);

        mockMvc.perform(delete("/api/v1/user-types/1"))
                .andExpect(status().isNoContent());

        verify(useCase, times(1)).delete(1L);
    }
}
