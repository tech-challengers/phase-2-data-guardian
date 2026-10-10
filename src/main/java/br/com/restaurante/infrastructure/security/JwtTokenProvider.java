package br.com.restaurante.infrastructure.security;

import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider implements TokenPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null");
        }

        String role = (user.getUserType() != null) ? user.getUserType().getName() : "";
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    @Override
    public AuthenticatedUser extractTokenData(String token) {
        Claims claims = parseClaims(token);

        Long userId;
        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Number number) {
            userId = number.longValue();
        } else if (claims.getSubject() != null) {
            userId = Long.valueOf(claims.getSubject());
        } else {
            throw new InvalidTokenException("Token does not contain user ID");
        }

        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);

        return new AuthenticatedUser(userId, email, name, role);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("Token cannot be null or empty");
        }

        String sanitizedToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(sanitizedToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid or malformed JWT token: " + e.getMessage(), e);
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
