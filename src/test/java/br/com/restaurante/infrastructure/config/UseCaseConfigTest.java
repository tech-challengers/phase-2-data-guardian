package br.com.restaurante.infrastructure.config;

import br.com.restaurante.application.ports.out.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UseCaseConfigTest {

    @Test
    void createBeans() {
        UseCaseConfig config = new UseCaseConfig();
        UserRepositoryPort userPort = mock(UserRepositoryPort.class);
        UserTypeRepositoryPort typePort = mock(UserTypeRepositoryPort.class);
        PasswordEncoderPort passwordEncoderPort = mock(PasswordEncoderPort.class);
        TokenPort tokenPort = mock(TokenPort.class);
        MenuItemRepositoryPort menuItemPort = mock(MenuItemRepositoryPort.class);
        RestauranteRepositoryPort restaurantePort = mock(RestauranteRepositoryPort.class);
        StoragePort storagePort = mock(StoragePort.class);

        assertNotNull(config.userTypeUseCase(typePort));
        assertNotNull(config.userUseCase(userPort, typePort, passwordEncoderPort));
        assertNotNull(config.authUseCase(userPort, passwordEncoderPort, tokenPort, 86400000L));
        assertNotNull(config.tokenUseCase(tokenPort));
        assertNotNull(config.menuItemUseCase(menuItemPort, restaurantePort, storagePort));
    }
}
