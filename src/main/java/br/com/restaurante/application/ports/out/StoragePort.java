package br.com.restaurante.application.ports.out;

import java.io.InputStream;

/**
 * Porta de saída para operações de armazenamento de arquivos (ex: imagens).
 * O domínio/serviço depende apenas desta interface — desacoplado de qualquer
 * provedor de armazenamento específico (Supabase, Firebase, AWS S3, etc.).
 */
public interface StoragePort {

    /**
     * Faz upload de um arquivo e retorna a URL pública permanente.
     *
     * @param inputStream conteúdo do arquivo
     * @param contentType MIME type (ex: "image/jpeg")
     * @param storagePath caminho dentro do bucket (ex: "menu-items/123/photo.jpg")
     * @return URL pública do arquivo armazenado
     */
    String upload(InputStream inputStream, String contentType, String storagePath);

    /**
     * Remove um arquivo do storage pelo caminho dentro do bucket ou pela sua URL pública.
     *
     * @param pathOrUrl caminho dentro do bucket (ex: "menu-items/123/photo.jpg") ou URL pública completa
     */
    void delete(String pathOrUrl);

    /**
     * Verifica se uma dada URL pertence ao serviço de storage gerenciado pela aplicação.
     *
     * @param url URL da imagem
     * @return true se pertencer ao storage gerenciado, false caso contrário
     */
    boolean isStorageUrl(String url);
}
