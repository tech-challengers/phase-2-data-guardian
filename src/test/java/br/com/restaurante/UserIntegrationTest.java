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

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserIntegrationTest extends AbstractMySqlIntegrationTest {

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

    private static String userBody(String name, String email, String password) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private long createUser(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userBody("Usuario Teste", email, password)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    @Test
    void registerWithoutTypeCannotLogin() throws Exception {
        String email = "sem-tipo-login-" + UUID.randomUUID() + "@email.com";
        createUser(email, "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAssignTypeThenLogin() throws Exception {
        String email = "novo-" + UUID.randomUUID() + "@email.com";
        long userId = createUser(email, "senha123");
        String admin = login("dono@restaurante.com", "admin123");

        MvcResult types = mockMvc.perform(get("/api/v1/user-types").header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn();
        List<Number> ids = JsonPath.read(types.getResponse().getContentAsString(),
                "$[?(@.name=='CLIENTE')].id");

        mockMvc.perform(put("/api/v1/users/" + userId + "/type")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\":" + ids.get(0).longValue() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userType.name").value("CLIENTE"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@email.com";
        createUser(email, "senha123");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userBody("Outro", email, "senha123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listUsersRequiresToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        String dono = login("dono@restaurante.com", "admin123");
        mockMvc.perform(get("/api/v1/users").header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='dono@restaurante.com')]").exists());
    }

    @Test
    void assignUserTypeChangesRole() throws Exception {
        String email = "tipo-" + UUID.randomUUID() + "@email.com";
        long userId = createUser(email, "senha123");
        String admin = login("dono@restaurante.com", "admin123");

        MvcResult types = mockMvc.perform(get("/api/v1/user-types").header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn();
        List<Number> ids = JsonPath.read(types.getResponse().getContentAsString(),
                "$[?(@.name=='CLIENTE')].id");

        mockMvc.perform(put("/api/v1/users/" + userId + "/type")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\":" + ids.get(0).longValue() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType.name").value("CLIENTE"));
    }

    @Test
    void assignUnknownUserTypeReturnsNotFound() throws Exception {
        String email = "sem-tipo-" + UUID.randomUUID() + "@email.com";
        long userId = createUser(email, "senha123");
        String admin = login("dono@restaurante.com", "admin123");

        mockMvc.perform(put("/api/v1/users/" + userId + "/type")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\":999999}"))
                .andExpect(status().isNotFound());
    }
}