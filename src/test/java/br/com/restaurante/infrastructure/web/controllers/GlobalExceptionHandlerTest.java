package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import br.com.restaurante.core.domain.exceptions.InvalidCredentialsException;
import br.com.restaurante.core.domain.exceptions.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_badRequest() {
        ResponseEntity<ProblemDetail> res = handler.handleIllegalArgument(new IllegalArgumentException("Some error"));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Some error", res.getBody().getDetail());
        assertEquals("Bad Request", res.getBody().getTitle());
        assertEquals("https://api.restaurante.com/errors/400", res.getBody().getType().toString());
    }

    @Test
    void handleIllegalArgument_notFound() {
        ResponseEntity<ProblemDetail> res = handler.handleIllegalArgument(new IllegalArgumentException("User not found"));
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("User not found", res.getBody().getDetail());
        assertEquals("Not Found", res.getBody().getTitle());
        assertEquals("https://api.restaurante.com/errors/404", res.getBody().getType().toString());
    }

    @Test
    void handleIllegalArgument_nullMessage() {
        ResponseEntity<ProblemDetail> res = handler.handleIllegalArgument(new IllegalArgumentException());
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertNull(res.getBody().getDetail());
    }

    @Test
    void handleIllegalState_returns500() {
        ResponseEntity<ProblemDetail> res = handler.handleIllegalState(new IllegalStateException("Erro interno de storage"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Erro interno de storage", res.getBody().getDetail());
        assertEquals("Internal Server Error", res.getBody().getTitle());
        assertEquals("https://api.restaurante.com/errors/500", res.getBody().getType().toString());
    }

    @Test
    void handleMaxUploadSize_returns400() {
        ResponseEntity<ProblemDetail> res = handler.handleMaxUploadSize(new MaxUploadSizeExceededException(10485760));
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertTrue(res.getBody().getDetail().contains("10 MB"));
        assertEquals("Bad Request", res.getBody().getTitle());
        assertEquals("https://api.restaurante.com/errors/400", res.getBody().getType().toString());
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ProblemDetail> res = handler.handleInvalidCredentials(new InvalidCredentialsException("Credenciais inválidas"));
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Credenciais inválidas", res.getBody().getDetail());
        assertEquals("Unauthorized", res.getBody().getTitle());
    }

    @Test
    void handleInvalidToken_returns401() {
        ResponseEntity<ProblemDetail> res = handler.handleInvalidToken(new InvalidTokenException("Token expirado"));
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Token expirado", res.getBody().getDetail());
        assertEquals("Unauthorized", res.getBody().getTitle());
    }

    @Test
    void handleAccessDenied_domainException_returns403() {
        ResponseEntity<ProblemDetail> res = handler.handleAccessDenied(new AccessDeniedDomainException("Acesso restrito a Dono"));
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Acesso restrito a Dono", res.getBody().getDetail());
        assertEquals("Forbidden", res.getBody().getTitle());
    }

    @Test
    void handleAccessDenied_springSecurityException_returns403() {
        ResponseEntity<ProblemDetail> res = handler.handleAccessDenied(new AccessDeniedException("Access Denied"));
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals("Access Denied", res.getBody().getDetail());
    }
}
