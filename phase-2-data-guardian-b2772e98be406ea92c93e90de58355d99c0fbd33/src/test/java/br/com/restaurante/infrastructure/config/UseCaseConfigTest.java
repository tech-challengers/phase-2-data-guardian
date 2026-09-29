package br.com.restaurante.infrastructure.config;

import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UseCaseConfigTest {

    @Test
    void createBeans() {
        UseCaseConfig config = new UseCaseConfig();
        UserRepositoryPort userPort = mock(UserRepositoryPort.class);
        UserTypeRepositoryPort typePort = mock(UserTypeRepositoryPort.class);

        assertNotNull(config.userTypeUseCase(typePort));
        assertNotNull(config.userUseCase(userPort, typePort));
    }
}
