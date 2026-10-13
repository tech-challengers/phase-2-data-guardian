package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.User;

public interface AuthUseCase {
    LoginResult login(String email, String password);

    record LoginResult(
            String accessToken,
            String tokenType,
            Long expiresIn,
            User user
    ) {}
}
