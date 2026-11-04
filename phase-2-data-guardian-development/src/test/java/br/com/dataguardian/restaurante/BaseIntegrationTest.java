package br.com.dataguardian.restaurante;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
public class BaseIntegrationTest {

    @Test
    void contextLoads() {
        assertTrue(true, "Application context and Testcontainers base are properly configured.");
    }
}
