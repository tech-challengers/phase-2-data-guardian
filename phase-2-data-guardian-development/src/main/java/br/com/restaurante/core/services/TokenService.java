package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.TokenUseCase;
import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.core.domain.AuthenticatedUser;
import br.com.restaurante.core.domain.User;

public class TokenService implements TokenUseCase {

    private final TokenPort tokenPort;

    public TokenService(TokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    @Override
    public String generateToken(User user) {
        return tokenPort.generateToken(user);
    }

    @Override
    public AuthenticatedUser extractUserFromToken(String token) {
        return tokenPort.extractTokenData(token);
    }

    @Override
    public boolean validateToken(String token) {
        return tokenPort.validateToken(token);
    }
}
