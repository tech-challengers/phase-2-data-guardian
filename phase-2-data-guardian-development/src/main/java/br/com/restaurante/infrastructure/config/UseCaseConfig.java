package br.com.restaurante.infrastructure.config;

import br.com.restaurante.application.ports.in.AuthUseCase;
import br.com.restaurante.application.ports.in.TokenUseCase;
import br.com.restaurante.application.ports.in.UserTypeUseCase;
import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.TokenPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.services.AuthService;
import br.com.restaurante.core.services.TokenService;
import br.com.restaurante.core.services.UserService;
import br.com.restaurante.core.services.UserTypeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UserTypeUseCase userTypeUseCase(UserTypeRepositoryPort port) {
        return new UserTypeService(port);
    }

    @Bean
    public UserUseCase userUseCase(
            UserRepositoryPort userPort,
            UserTypeRepositoryPort typePort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        return new UserService(userPort, typePort, passwordEncoderPort);
    }

    @Bean
    public AuthUseCase authUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenPort tokenPort,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        return new AuthService(userRepositoryPort, passwordEncoderPort, tokenPort, expirationMs);
    }

    @Bean
    public TokenUseCase tokenUseCase(TokenPort tokenPort) {
        return new TokenService(tokenPort);
    }
}
