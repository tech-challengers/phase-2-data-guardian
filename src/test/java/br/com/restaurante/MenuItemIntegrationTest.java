package br.com.restaurante;

import br.com.restaurante.application.ports.out.StoragePort;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuItemIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final String SUPABASE_URL = "https://xyz.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.jpg";

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private StoragePort storagePort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        if (storagePort != null) {
            when(storagePort.upload(any(), any(), any()))
                    .thenReturn(SUPABASE_URL);
        }
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

        // dono cria com URL via JSON -> 201
        MvcResult created = mockMvc.perform(post(itemsPath)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada", "49.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.restaurantId").value(restaurantId))
                .andExpect(jsonPath("$.name").value("Feijoada"))
                .andExpect(jsonPath("$.photoPath").value("/img/f.jpg"))
                .andReturn();
        Number itemIdNumber = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
        long itemId = itemIdNumber.longValue();
        String itemPath = "/api/v1/menu-items/" + itemId;

        // dono cria segundo item com arquivo de imagem via multipart/form-data -> 201
        MockMultipartFile photo = new MockMultipartFile("photo", "prato.jpg", "image/jpeg", "fake-img".getBytes());
        mockMvc.perform(multipart(itemsPath + "/with-photo")
                        .file(photo)
                        .param("data", itemBody("Moqueca", "65.00"))
                        .header("Authorization", dono))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Moqueca"))
                .andExpect(jsonPath("$.photoPath").value(SUPABASE_URL));

        // listagem deve retornar os 2 itens
        mockMvc.perform(get(itemsPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get(itemPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijoada"));

        // cliente tenta atualizar -> 403
        mockMvc.perform(put(itemPath)
                        .header("Authorization", cliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Hack", "1")))
                .andExpect(status().isForbidden());

        // cliente tenta deletar -> 403
        mockMvc.perform(delete(itemPath).header("Authorization", cliente))
                .andExpect(status().isForbidden());

        // dono edita via JSON -> 200
        mockMvc.perform(put(itemPath)
                        .header("Authorization", dono)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemBody("Feijoada Premium", "59.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feijoada Premium"))
                .andExpect(jsonPath("$.price").value(59.00));

        // dono faz upload de foto avulsa para o item -> 200
        mockMvc.perform(multipart(itemPath + "/photo")
                        .file(photo)
                        .header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoPath").value(SUPABASE_URL));

        // dono remove a foto do item -> 200
        mockMvc.perform(delete(itemPath + "/photo")
                        .header("Authorization", dono))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoPath").doesNotExist());

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
