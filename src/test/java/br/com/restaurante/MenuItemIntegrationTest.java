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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemIntegrationTest extends AbstractMySqlIntegrationTest {

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
        assertNotNull(token);
        return "Bearer " + token;
    }

    private long createRestaurant(String bearer) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/restaurantes")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Restaurante da Praça",
                                  "endereco": "Rua das Flores, 100",
                                  "gastronomia": "Brasileira",
                                  "horarioFuncionamento": "11:00 às 22:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private static String itemBody(String name, String price) {
        return """
                {
                  "name": "%s",
                  "description": "Completa",
                  "price": %s,
                  "dineInOnly": false,
                  "photoPath": "/img/f.jpg"
                }
                """.formatted(name, price);
    }

    @Test
    void fullMenuItemFlowWithOwnerRules() throws Exception {
        String dono = login("dono@restaurante.com", "admin123");
        String cliente = login("cliente@email.com", "cliente123");
        long restaurantId = createRestaurant(dono);
        String itemsPath = "/api/v1/restaurants/" + restaurantId + "/menu-items";

        // sem token -> 401
        mockMvc.perform(post(itemsPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada", "49.90")))
                .andExpect(status().isUnauthorized());

        // cliente -> 403
        mockMvc.perform(post(itemsPath)
                        .header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada", "49.90")))
                .andExpect(status().isForbidden());

        // dono cria -> 201
        MvcResult created = mockMvc.perform(post(itemsPath)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada", "49.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.name").value("Feijoada"))
                .andReturn();
        Number itemIdNumber = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        long itemId = itemIdNumber.longValue();
        String itemPath = "/api/v1/menu-items/" + itemId;

        mockMvc.perform(get(itemsPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get(itemPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijoada"));

        mockMvc.perform(put(itemPath)
                        .header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Hack", "1")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(itemPath).header("Authorization", cliente))
                .andExpect(status().isForbidden());

        // dono edita -> 200
        mockMvc.perform(put(itemPath)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Moqueca", "59.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Moqueca"))
                .andExpect(jsonPath("$.price").value(59.00));

        // dados inválidos -> 400
        mockMvc.perform(post(itemsPath)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Ruim", "-5")))
                .andExpect(status().isBadRequest());

        // restaurante inexistente -> 404
        mockMvc.perform(post("/api/v1/restaurants/999999/menu-items")
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada", "49.90")))
                .andExpect(status().isNotFound());

        // dono apaga -> 204, depois 404
        mockMvc.perform(delete(itemPath).header("Authorization", dono))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(itemPath))
                .andExpect(status().isNotFound());
    }
}