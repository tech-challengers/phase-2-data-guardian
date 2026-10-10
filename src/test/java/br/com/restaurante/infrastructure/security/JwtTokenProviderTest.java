package br.com.restaurante.infrastructure.security;

import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.core.domain.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, 86400000L);
    }

    @Test
    void generateAndExtract_donoDeRestaurante_success() {
        User user = new User(10L, "Carlos Dono", "carlos@restaurante.com", "pass",
                new UserType(1L, UserType.DONO_DE_RESTAURANTE));

        String token = tokenProvider.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isBlank());

        assertTrue(tokenProvider.validateToken(token));

        AuthenticatedUser extracted = tokenProvider.extractTokenData(token);
        assertEquals(10L, extracted.getId());
        assertEquals("carlos@restaurante.com", extracted.getEmail());
        assertEquals("Carlos Dono", extracted.getName());
        assertEquals(UserType.DONO_DE_RESTAURANTE, extracted.getRole());
        assertTrue(extracted.isDonoDeRestaurante());
        assertFalse(extracted.isCliente());
    }

    @Test
    void generateAndExtract_cliente_success() {
        User user = new User(20L, "Ana Cliente", "ana@email.com", "pass",
                new UserType(2L, UserType.CLIENTE));

        String token = tokenProvider.generateToken(user);
        assertNotNull(token);

        assertTrue(tokenProvider.validateToken("Bearer " + token));

        AuthenticatedUser extracted = tokenProvider.extractTokenData("Bearer " + token);
        assertEquals(20L, extracted.getId());
        assertEquals("ana@email.com", extracted.getEmail());
        assertEquals("Ana Cliente", extracted.getName());
        assertEquals(UserType.CLIENTE, extracted.getRole());
        assertTrue(extracted.isCliente());
        assertFalse(extracted.isDonoDeRestaurante());
    }

    @Test
    void generateToken_nullUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.generateToken(null));
        User userWithoutId = new User(null, "Test", "t@t.com", null);
        assertThrows(IllegalArgumentException.class, () -> tokenProvider.generateToken(userWithoutId));
    }

    @Test
    void validateToken_emptyOrNullToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken(null));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken("   "));
    }

    @Test
    void extractTokenData_emptyOrNullToken_throwsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData(null));
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData(""));
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData("   "));
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertFalse(tokenProvider.validateToken("invalid.token.string"));
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData("invalid.token.string"));
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        User user = new User(1L, "Test", "t@t.com", "pass", new UserType(1L, UserType.CLIENTE));
        String validToken = tokenProvider.generateToken(user);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "abcde";

        assertFalse(tokenProvider.validateToken(tamperedToken));
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData(tamperedToken));
    }

    @Test
    void validateToken_differentSecretKey_failsValidation() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "DifferentSecretKeyForTestingPurposesMustBeLongEnough1234567890", 86400000L);
        User user = new User(1L, "Test", "t@t.com", "pass", new UserType(1L, UserType.CLIENTE));
        String tokenFromOther = otherProvider.generateToken(user);

        assertFalse(tokenProvider.validateToken(tokenFromOther));
        assertThrows(InvalidTokenException.class, () -> tokenProvider.extractTokenData(tokenFromOther));
    }

    @Test
    void validateToken_expiredToken_throwsInvalidTokenException() throws InterruptedException {
        // Create token provider with 1 millisecond expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, 1L);
        User user = new User(1L, "Expiring", "exp@t.com", "pass", new UserType(1L, UserType.CLIENTE));
        String token = shortLivedProvider.generateToken(user);

        // Wait 50ms to ensure expiry
        Thread.sleep(50);

        assertFalse(shortLivedProvider.validateToken(token));
        InvalidTokenException ex = assertThrows(InvalidTokenException.class,
                () -> shortLivedProvider.extractTokenData(token));
        assertTrue(ex.getMessage().contains("expired"));
    }
}
