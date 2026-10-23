package br.com.restaurante;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationStartupIntegrationTest extends AbstractMySqlIntegrationTest {

    @Test
    void applicationStartsSuccessfullyWithTestcontainers() {
        assertTrue(MYSQL.isRunning(), "MySQL testcontainer should be running");
    }
}