package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.User;

public interface TokenPort {
    String generateToken(User user);
    AuthenticatedUser extractTokenData(String token);
    boolean validateToken(String token);
}
