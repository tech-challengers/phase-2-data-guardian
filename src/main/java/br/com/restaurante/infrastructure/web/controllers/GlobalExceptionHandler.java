package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.core.domain.exceptions.AccessDeniedDomainException;
import br.com.restaurante.core.domain.exceptions.InvalidCredentialsException;
import br.com.restaurante.core.domain.exceptions.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found"))
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("https://api.restaurante.com/errors/" + status.value()));

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(URI.create("https://api.restaurante.com/errors/401"));
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidToken(InvalidTokenException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Unauthorized");
        problemDetail.setType(URI.create("https://api.restaurante.com/errors/401"));
        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler({AccessDeniedDomainException.class, AccessDeniedException.class})
    public ResponseEntity<ProblemDetail> handleAccessDenied(Exception ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setTitle("Forbidden");
        problemDetail.setType(URI.create("https://api.restaurante.com/errors/403"));
        return ResponseEntity.status(status).body(problemDetail);
    }
}
