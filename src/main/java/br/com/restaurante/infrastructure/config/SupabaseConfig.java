package br.com.restaurante.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuração de integração com o Supabase Storage via REST API.
 * <p>
 * Variáveis de ambiente:
 * SUPABASE_URL             — URL do projeto Supabase (ex: https://meu-projeto.supabase.co)
 * SUPABASE_KEY             — Service Role Key ou Anon Key
 * SUPABASE_STORAGE_BUCKET   — Nome do bucket de storage (default: menu-items)
 */
@Configuration
public class SupabaseConfig {

    @Value("${supabase.url:https://test-project.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    public RestClient supabaseRestClient() {
        String baseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";

        // O .trim() remove eventuais quebras de linha invisíveis ao copiar e colar a chave
        String cleanKey = supabaseKey != null ? supabaseKey.trim() : "";

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + (supabaseKey != null ? supabaseKey : ""))
                .defaultHeader("apikey", cleanKey)
                .build();
    }
}
