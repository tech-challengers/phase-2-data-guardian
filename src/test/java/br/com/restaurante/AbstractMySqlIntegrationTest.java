package br.com.restaurante;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.junit.jupiter.api.condition.EnabledIf;

@SpringBootTest
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker não disponível")
public abstract class AbstractMySqlIntegrationTest {

    static MySQLContainer<?> MYSQL;

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    static {
        if (dockerAvailable()) {
            MYSQL = new MySQLContainer<>("mysql:8.0.36")
                    .withDatabaseName("db_restaurante")
                    .withUsername("test")
                    .withPassword("test");
            MYSQL.start();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (MYSQL == null) return;
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }
}