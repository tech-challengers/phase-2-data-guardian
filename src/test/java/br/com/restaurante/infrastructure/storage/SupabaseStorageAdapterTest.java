package br.com.restaurante.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SupabaseStorageAdapterTest {

    private static final String SUPABASE_URL = "https://meu-projeto.supabase.co";
    private static final String BUCKET = "menu-items";

    private MockRestServiceServer mockServer;
    private SupabaseStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl(SUPABASE_URL).build();
        adapter = new SupabaseStorageAdapter(SUPABASE_URL, BUCKET, restClient);
    }

    @Test
    @DisplayName("upload deve enviar os bytes para o Supabase Storage e retornar a URL pública permanente")
    void uploadShouldPostBytesAndReturnPublicUrl() {
        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items/menu-items/1/photo.jpg"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-upsert", "true"))
                .andExpect(header("Content-Type", "image/jpeg"))
                .andExpect(content().bytes("imagem-bytes".getBytes()))
                .andRespond(withSuccess("{\"Key\":\"menu-items/1/photo.jpg\"}", MediaType.APPLICATION_JSON));

        InputStream stream = new ByteArrayInputStream("imagem-bytes".getBytes());
        String url = adapter.upload(stream, "image/jpeg", "menu-items/1/photo.jpg");

        mockServer.verify();
        assertEquals("https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.jpg", url);
    }

    @Test
    @DisplayName("upload com contentType nulo deve usar fallback application/octet-stream")
    void uploadWithNullContentTypeShouldFallbackToOctetStream() {
        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items/menu-items/1/photo.png"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", "application/octet-stream"))
                .andRespond(withSuccess());

        InputStream stream = new ByteArrayInputStream("dados".getBytes());
        String url = adapter.upload(stream, null, "/menu-items/1/photo.png");

        mockServer.verify();
        assertEquals("https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.png", url);
    }

    @Test
    @DisplayName("upload deve lançar IllegalStateException quando o servidor do Supabase retornar erro")
    void uploadShouldThrowIllegalStateExceptionOnServerError() {
        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items/menu-items/1/photo.jpg"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        InputStream stream = new ByteArrayInputStream("erro".getBytes());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.upload(stream, "image/jpeg", "menu-items/1/photo.jpg"));

        assertTrue(ex.getMessage().contains("Falha ao fazer upload da imagem no Supabase Storage"));
    }

    @Test
    @DisplayName("delete com caminho relativo deve enviar DELETE com body prefixes")
    void deleteWithRelativePathShouldSendDeleteRequest() {
        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"prefixes\":[\"menu-items/1/photo.jpg\"]}"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        adapter.delete("menu-items/1/photo.jpg");

        mockServer.verify();
    }

    @Test
    @DisplayName("delete com URL pública completa deve extrair o caminho e enviar DELETE")
    void deleteWithPublicUrlShouldExtractPathAndSendDeleteRequest() {
        String publicUrl = "https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.jpg";

        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(content().json("{\"prefixes\":[\"menu-items/1/photo.jpg\"]}"))
                .andRespond(withSuccess());

        adapter.delete(publicUrl);

        mockServer.verify();
    }

    @Test
    @DisplayName("delete com URL contendo query params deve remover parâmetros antes de deletar")
    void deleteWithUrlContainingQueryParamsShouldStripQuery() {
        String urlWithQuery = "https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.jpg?t=123456";

        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(content().json("{\"prefixes\":[\"menu-items/1/photo.jpg\"]}"))
                .andRespond(withSuccess());

        adapter.delete(urlWithQuery);

        mockServer.verify();
    }

    @Test
    @DisplayName("delete com URL autenticada/privada deve extrair o caminho corretamente")
    void deleteWithPrivateUrlShouldExtractPath() {
        String privateUrl = "https://meu-projeto.supabase.co/storage/v1/object/menu-items/menu-items/2/photo.png";

        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(content().json("{\"prefixes\":[\"menu-items/2/photo.png\"]}"))
                .andRespond(withSuccess());

        adapter.delete(privateUrl);

        mockServer.verify();
    }

    @Test
    @DisplayName("delete com caminho nulo ou em branco não deve fazer requisições")
    void deleteWithNullOrBlankShouldDoNothing() {
        adapter.delete(null);
        adapter.delete("");
        adapter.delete("   ");
        // Nenhuma expectativa no mockServer, se fizer request o mockServer falha
    }

    @Test
    @DisplayName("delete com URL externa que não é do Supabase não deve fazer requisições")
    void deleteWithExternalUrlShouldDoNothing() {
        adapter.delete("https://siteexterno.com/foto.jpg");
        adapter.delete("http://outrodominio.com.br/imagem.png");
        // Nenhuma chamada ao storage
    }

    @Test
    @DisplayName("delete deve lançar IllegalStateException quando o servidor do Supabase retornar erro")
    void deleteShouldThrowIllegalStateExceptionOnServerError() {
        mockServer.expect(requestTo(SUPABASE_URL + "/storage/v1/object/menu-items"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withServerError());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.delete("menu-items/1/photo.jpg"));

        assertTrue(ex.getMessage().contains("Falha ao excluir imagem do Supabase Storage"));
    }

    @Test
    @DisplayName("isStorageUrl deve identificar URLs do Supabase corretamente")
    void isStorageUrlShouldIdentifyCorrectly() {
        assertTrue(adapter.isStorageUrl("https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/foto.jpg"));
        assertTrue(adapter.isStorageUrl("https://meu-projeto.supabase.co/storage/v1/object/menu-items/foto.jpg"));
        assertTrue(adapter.isStorageUrl("https://meu-projeto.supabase.co/outro-recurso"));

        assertFalse(adapter.isStorageUrl("https://google.com/imagem.png"));
        assertFalse(adapter.isStorageUrl("https://firebasestorage.googleapis.com/v0/b/bucket/o/img.jpg"));
        assertFalse(adapter.isStorageUrl(null));
        assertFalse(adapter.isStorageUrl(""));
        assertFalse(adapter.isStorageUrl("   "));
    }

    @Test
    @DisplayName("extractStoragePath e buildPublicUrl com casos limites")
    void helperMethodsEdgeCases() {
        assertEquals("", adapter.extractStoragePath(null));
        assertEquals("", adapter.extractStoragePath(""));
        assertEquals("foto.jpg", adapter.extractStoragePath("/foto.jpg"));
        assertEquals("menu-items/1/photo.jpg", adapter.extractStoragePath("menu-items/1/photo.jpg"));

        assertEquals("https://meu-projeto.supabase.co/storage/v1/object/public/menu-items/menu-items/1/photo.jpg",
                adapter.buildPublicUrl("/menu-items/1/photo.jpg"));
    }

    @Test
    @DisplayName("Construtor deve tratar fallback de bucket nulo e barra final na URL")
    void constructorShouldHandleFallbacks() {
        RestClient restClient = RestClient.builder().build();
        SupabaseStorageAdapter customAdapter = new SupabaseStorageAdapter("https://outro.supabase.co///", null, restClient);

        assertEquals("https://outro.supabase.co/storage/v1/object/public/menu-items/foto.jpg",
                customAdapter.buildPublicUrl("foto.jpg"));

        SupabaseStorageAdapter blankBucketAdapter = new SupabaseStorageAdapter(null, "   ", restClient);
        assertEquals("/storage/v1/object/public/menu-items/foto.jpg",
                blankBucketAdapter.buildPublicUrl("foto.jpg"));
    }
}
