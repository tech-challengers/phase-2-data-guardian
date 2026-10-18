package br.com.restaurante.infrastructure.persistence;

import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserTypeRepositoryPort userTypeRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public DataInitializer(
            UserTypeRepositoryPort userTypeRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        this.userTypeRepositoryPort = userTypeRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public void run(String... args) {
        log.info("Starting DataInitializer: Checking initial UserTypes and Users...");

        // 1. Seed DONO_DE_RESTAURANTE
        UserType donoType = userTypeRepositoryPort.findByName(UserType.DONO_DE_RESTAURANTE)
                .orElseGet(() -> {
                    log.info("Creating default UserType: {}", UserType.DONO_DE_RESTAURANTE);
                    return userTypeRepositoryPort.save(new UserType(null, UserType.DONO_DE_RESTAURANTE));
                });

        // 2. Seed CLIENTE
        UserType clienteType = userTypeRepositoryPort.findByName(UserType.CLIENTE)
                .orElseGet(() -> {
                    log.info("Creating default UserType: {}", UserType.CLIENTE);
                    return userTypeRepositoryPort.save(new UserType(null, UserType.CLIENTE));
                });

        // 3. Seed default DONO_DE_RESTAURANTE user
        String donoEmail = "dono@restaurante.com";
        userRepositoryPort.findByEmail(donoEmail)
                .orElseGet(() -> {
                    log.info("Creating default Dono user: {}", donoEmail);
                    User user = new User(
                            null,
                            "Dono do Restaurante",
                            donoEmail,
                            passwordEncoderPort.encode("admin123"),
                            donoType
                    );
                    return userRepositoryPort.save(user);
                });

        // 4. Seed default CLIENTE user
        String clienteEmail = "cliente@email.com";
        userRepositoryPort.findByEmail(clienteEmail)
                .orElseGet(() -> {
                    log.info("Creating default Cliente user: {}", clienteEmail);
                    User user = new User(
                            null,
                            "Cliente Teste",
                            clienteEmail,
                            passwordEncoderPort.encode("cliente123"),
                            clienteType
                    );
                    return userRepositoryPort.save(user);
                });

        log.info("DataInitializer completed successfully.");
    }
}
