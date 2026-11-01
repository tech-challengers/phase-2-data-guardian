package br.com.restaurante.infrastructure.storage;

import br.com.restaurante.application.ports.out.StoragePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de saída que implementa {@link StoragePort} usando a REST API do Supabase Storage.
 * Pertence à camada de infraestrutura — a camada de domínio e casos de uso nunca dependem desta classe.
 */
@Component
public class SupabaseStorageAdapter implements StoragePort {

    private final String supabaseUrl;
    private final String bucketName;
    private final RestClient restClient;

    public SupabaseStorageAdapter(
            @Value("${supabase.url:https://test-project.supabase.co}") String supabaseUrl,
            @Value("${supabase.storage-bucket:menu-items}") String bucketName,
            @Qualifier("supabaseRestClient") RestClient restClient
    ) {
        this.supabaseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";
        this.bucketName = (bucketName != null && !bucketName.isBlank()) ? bucketName : "menu-items";
        this.restClient = restClient;
    }

    @Override
    public String upload(InputStream inputStream, String contentType, String storagePath) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            String cleanPath = sanitizePath(storagePath);
            String uploadUri = "/storage/v1/object/" + bucketName + "/" + cleanPath;

            restClient.post()
                    .uri(uploadUri)
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .header("x-upsert", "true")
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();

            return buildPublicUrl(cleanPath);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao fazer upload da imagem no Supabase Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return;
        }

        // Se for uma URL externa que não pertence ao Supabase Storage, ignora
        if ((pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) && !isStorageUrl(pathOrUrl)) {
            return;
        }

        try {
            String cleanPath = extractStoragePath(pathOrUrl);
            String deleteUri = "/storage/v1/object/" + bucketName;

            Map<String, List<String>> requestBody = Map.of("prefixes", List.of(cleanPath));

            restClient.method(HttpMethod.DELETE)
                    .uri(deleteUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao excluir imagem do Supabase Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isStorageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return url.contains("/storage/v1/object/public/" + bucketName)
                || url.contains("/storage/v1/object/" + bucketName)
                || (!supabaseUrl.isBlank() && url.startsWith(supabaseUrl));
    }

    public String buildPublicUrl(String storagePath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + sanitizePath(storagePath);
    }

    public String extractStoragePath(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return "";
        }
        String path = pathOrUrl;
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }

        String publicPrefix = "/storage/v1/object/public/" + bucketName + "/";
        String privatePrefix = "/storage/v1/object/" + bucketName + "/";

        if (path.contains(publicPrefix)) {
            path = path.substring(path.indexOf(publicPrefix) + publicPrefix.length());
        } else if (path.contains(privatePrefix)) {
            path = path.substring(path.indexOf(privatePrefix) + privatePrefix.length());
        }

        return sanitizePath(path);
    }

    private String sanitizePath(String path) {
        if (path == null) {
            return "";
        }
        return path.replaceAll("^/+", "");
    }
}
