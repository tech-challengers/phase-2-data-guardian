package br.com.restaurante.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter();

    @Test
    void encodeAndMatch_success() {
        String raw = "mySecretPassword123";
        String encoded = adapter.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded);
        assertTrue(adapter.matches(raw, encoded));
        assertFalse(adapter.matches("wrongPass", encoded));
    }

    @Test
    void nullHandling() {
        assertNull(adapter.encode(null));
        assertFalse(adapter.matches(null, "someHash"));
        assertFalse(adapter.matches("pass", null));
    }
}
