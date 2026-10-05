package br.com.restaurante.infrastructure.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

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
}
