package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.infrastructure.web.dto.AssignUserTypeRequest;
import br.com.restaurante.infrastructure.web.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserUseCase useCase;
    private UserController controller;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        useCase = mock(UserUseCase.class);
        controller = new UserController(useCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createUser_success() throws Exception {
        UserRequest req = new UserRequest();
        req.setName("Maria");
        req.setEmail("maria@test.com");

        when(useCase.create(any())).thenReturn(new User(1L, "Maria", "maria@test.com", null));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@test.com"));

        verify(useCase, times(1)).create(any());
    }

    @Test
    void createUser_duplicateEmail_returnsProblemDetailBadRequest() throws Exception {
        UserRequest req = new UserRequest();
        req.setName("Maria");
        req.setEmail("maria@test.com");

        when(useCase.create(any())).thenThrow(new IllegalArgumentException("Email already in use"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Email already in use"));
    }

    @Test
    void listUsers_success() throws Exception {
        User u1 = new User(1L, "Bob", "b@b.com", new UserType(1L, "Dono de Restaurante"));
        when(useCase.findAll()).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Bob"))
                .andExpect(jsonPath("$[0].userType.name").value("Dono de Restaurante"));

        verify(useCase, times(1)).findAll();
    }

    @Test
    void assignUserType_success() throws Exception {
        AssignUserTypeRequest req = new AssignUserTypeRequest();
        req.setUserTypeId(1L);

        User updated = new User(1L, "Bob", "b@b.com", new UserType(1L, "Dono de Restaurante"));
        when(useCase.assignUserType(1L, 1L)).thenReturn(updated);

        mockMvc.perform(put("/api/v1/users/1/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType.id").value(1L))
                .andExpect(jsonPath("$.userType.name").value("Dono de Restaurante"));

        verify(useCase, times(1)).assignUserType(1L, 1L);
    }

    @Test
    void assignUserType_notFound_returnsProblemDetailNotFound() throws Exception {
        AssignUserTypeRequest req = new AssignUserTypeRequest();
        req.setUserTypeId(99L);

        when(useCase.assignUserType(1L, 99L)).thenThrow(new IllegalArgumentException("UserType not found"));

        mockMvc.perform(put("/api/v1/users/1/type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("UserType not found"));
    }

    @Test
    void getUserById_success() throws Exception {
        User user = new User(1L, "Alice", "alice@example.com", null);
        when(useCase.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void updateUser_success() throws Exception {
        UserRequest req = new UserRequest();
        req.setName("Alice Updated");
        req.setEmail("alice2@example.com");

        User updatedUser = new User(1L, "Alice Updated", "alice2@example.com", null);
        when(useCase.update(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.email").value("alice2@example.com"));
    }

    @Test
    void deleteUser_success() throws Exception {
        doNothing().when(useCase).delete(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(useCase, times(1)).delete(1L);
    }
}