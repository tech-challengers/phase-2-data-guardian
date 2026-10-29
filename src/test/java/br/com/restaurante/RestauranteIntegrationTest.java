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

class RestauranteIntegrationTest extends AbstractMySqlIntegrationTest {

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

    private static String body(String nome) {
        return """
                {
                  "nome": "%s",
                  "endereco": "Rua das Flores, 100",
                  "gastronomia": "Brasileira",
                  "horarioFuncionamento": "11:00 às 22:00"
                }
                """.formatted(nome);
    }

    private long createRestaurant(String bearer, String nome) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/restaurantes")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(nome)))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private String createSecondOwner() throws Exception {
        String email = "dono2-" + UUID.randomUUID() + "@restaurante.com";
        String password = "senha123";

        MvcResult created = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dono Dois\",\"email\":\"" + email
                                + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Number userId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        String admin = login("dono@restaurante.com", "admin123");
        MvcResult types = mockMvc.perform(get("/api/v1/user-types")
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andReturn();
        java.util.List<Number> ids = JsonPath.read(types.getResponse().getContentAsString(),
                "$[?(@.name=='DONO_DE_RESTAURANTE')].id");
        Number ownerTypeId = ids.get(0);

        mockMvc.perform(put("/api/v1/users/" + userId.longValue() + "/type")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userTypeId\":" + ownerTypeId.longValue() + "}"))
                .andExpect(status().isOk());

        return login(email, password);
    }

    @Test
    void ownerCreatesAndReadsRestaurant() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        long id = createRestaurant(dono, "Restaurante Criado");

        mockMvc.perform(get("/api/v1/restaurantes/" + id).header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Restaurante Criado"));

        mockMvc.perform(get("/api/v1/restaurantes").header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + id + ")]").exists());
    }

    @Test
    void clientCannotCreateRestaurant() throws Exception {
        String cliente = login("cliente@email.com", "cliente123");

        mockMvc.perform(post("/api/v1/restaurantes")
                        .header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Nao deveria criar")))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Sem token")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/restaurantes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blankNameIsRejected() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        mockMvc.perform(post("/api/v1/restaurantes")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerUpdatesOwnRestaurant() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        long id = createRestaurant(dono, "Antes");

        mockMvc.perform(put("/api/v1/restaurantes/" + id)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Depois")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Depois"));
    }

    @Test
    void anotherOwnerCannotUpdateOrDeleteRestaurant() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        long id = createRestaurant(dono, "Do primeiro dono");
        String outroDono = createSecondOwner();

        mockMvc.perform(put("/api/v1/restaurantes/" + id)
                        .header("Authorization", outroDono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Invasao")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/restaurantes/" + id)
                        .header("Authorization", outroDono))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/restaurantes/" + id).header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Do primeiro dono"));
    }

    @Test
    void ownerDeletesOwnRestaurant() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        long id = createRestaurant(dono, "Para apagar");

        mockMvc.perform(delete("/api/v1/restaurantes/" + id).header("Authorization", dono))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/restaurantes/" + id).header("Authorization", dono))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void unknownRestaurantReturnsClientError() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");

        mockMvc.perform(get("/api/v1/restaurantes/999999").header("Authorization", dono))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(delete("/api/v1/restaurantes/999999").header("Authorization", dono))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(put("/api/v1/restaurantes/999999")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Fantasma")))
                .andExpect(status().isForbidden());
    }
}