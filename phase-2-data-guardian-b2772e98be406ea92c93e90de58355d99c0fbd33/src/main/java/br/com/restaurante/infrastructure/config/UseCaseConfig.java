package br.com.restaurante.infrastructure.config;

import br.com.restaurante.application.ports.in.UserTypeUseCase;
import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.services.UserService;
import br.com.restaurante.core.services.UserTypeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UserTypeUseCase userTypeUseCase(UserTypeRepositoryPort port) {
        return new UserTypeService(port);
    }

    @Bean
    public UserUseCase userUseCase(UserRepositoryPort userPort, UserTypeRepositoryPort typePort) {
        return new UserService(userPort, typePort);
    }
}
