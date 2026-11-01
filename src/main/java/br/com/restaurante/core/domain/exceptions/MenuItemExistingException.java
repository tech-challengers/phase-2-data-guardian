package br.com.restaurante.core.domain.exceptions;

public class MenuItemExistingException extends RuntimeException {
    public MenuItemExistingException(String message) {
        super(message);
    }

    public MenuItemExistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
