package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.exceptions.InvalidCredentialsException;

public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenPort tokenPort;
    private final long tokenExpirationMs;

    public AuthService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenPort tokenPort,
            long tokenExpirationMs
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenPort = tokenPort;
        this.tokenExpirationMs = tokenExpirationMs;
    }

    public AuthService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenPort tokenPort
    ) {
        this(userRepositoryPort, passwordEncoderPort, tokenPort, 86400000L);
    }

    @Override
    public LoginResult login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new InvalidCredentialsException("Email e senha são obrigatórios");
        }

        User user = userRepositoryPort.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (user.getPassword() == null || !passwordEncoderPort.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        if (user.getUserType() == null) {
            throw new InvalidCredentialsException("Usuário não possui UserType associado");
        }

        String token = tokenPort.generateToken(user);
        return new LoginResult(token, "Bearer", tokenExpirationMs, user);
    }
}
