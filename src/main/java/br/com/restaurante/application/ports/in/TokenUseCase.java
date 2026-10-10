package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.User;

public interface TokenUseCase {
    String generateToken(User user);
    AuthenticatedUser extractUserFromToken(String token);
    boolean validateToken(String token);
}
