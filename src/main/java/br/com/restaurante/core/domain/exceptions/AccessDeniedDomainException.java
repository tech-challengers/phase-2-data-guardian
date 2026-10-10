package br.com.restaurante.core.domain.exceptions;

public class AccessDeniedDomainException extends RuntimeException {
    public AccessDeniedDomainException(String message) {
        super(message);
    }
}
