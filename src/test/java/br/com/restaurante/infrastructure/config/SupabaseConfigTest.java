package br.com.restaurante.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SupabaseConfigTest {

    @Test
    @DisplayName("supabaseRestClient deve criar instância de RestClient com propriedades configuradas")
    void supabaseRestClientShouldCreateClient() {
        SupabaseConfig config = new SupabaseConfig();
        ReflectionTestUtils.setField(config, "supabaseUrl", "https://xyz.supabase.co");
        ReflectionTestUtils.setField(config, "supabaseKey", "secret-key");

        RestClient client = config.supabaseRestClient();

        assertNotNull(client);
    }

    @Test
    @DisplayName("supabaseRestClient deve tratar URLs nulas e chaves nulas sem lançar erro")
    void supabaseRestClientShouldHandleNullProperties() {
        SupabaseConfig config = new SupabaseConfig();
        ReflectionTestUtils.setField(config, "supabaseUrl", null);
        ReflectionTestUtils.setField(config, "supabaseKey", null);

        RestClient client = config.supabaseRestClient();

        assertNotNull(client);
    }
}
