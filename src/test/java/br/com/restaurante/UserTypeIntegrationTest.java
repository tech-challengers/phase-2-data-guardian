package br.com.restaurante;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserTypeIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        return "Bearer " + token;
    }

    private static String body(String name) {
        return "{\"name\":\"" + name + "\"}";
    }

    private long createType(String bearer, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/user-types")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(name)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void routesRequireToken() throws Exception {
        mockMvc.perform(get("/api/v1/user-types"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/user-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("SEM_TOKEN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listContainsDefaultTypes() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        mockMvc.perform(get("/api/v1/user-types").header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='DONO_DE_RESTAURANTE')]").exists())
                .andExpect(jsonPath("$[?(@.name=='CLIENTE')]").exists());
    }

    @Test
    void createAndGetById() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        String name = "TIPO_" + UUID.randomUUID();

        long id = createType(dono, name);

        mockMvc.perform(get("/api/v1/user-types/" + id).header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void duplicateNameIsRejected() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        String name = "DUP_" + UUID.randomUUID();
        createType(dono, name);

        mockMvc.perform(post("/api/v1/user-types")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(name)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        mockMvc.perform(post("/api/v1/user-types")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChangesName() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        long id = createType(dono, "ANTES_" + UUID.randomUUID());
        String newName = "DEPOIS_" + UUID.randomUUID();

        mockMvc.perform(put("/api/v1/user-types/" + id)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(newName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName));

        mockMvc.perform(get("/api/v1/user-types/" + id).header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName));
    }

    @Test
    void deleteRemovesType() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        long id = createType(dono, "APAGAR_" + UUID.randomUUID());

        mockMvc.perform(delete("/api/v1/user-types/" + id).header("Authorization", dono))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/user-types/" + id).header("Authorization", dono))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownTypeReturnsNotFound() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        mockMvc.perform(get("/api/v1/user-types/999999").header("Authorization", dono))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/user-types/999999")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("QUALQUER_" + UUID.randomUUID())))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/user-types/999999").header("Authorization", dono))
                .andExpect(status().isNotFound());
    }
}