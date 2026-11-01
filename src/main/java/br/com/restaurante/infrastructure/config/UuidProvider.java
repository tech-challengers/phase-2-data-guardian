package br.com.restaurante.infrastructure.config;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidProvider {

    // Instância thread-safe do gerador UUIDv7 (time-based epoch random)
    private final TimeBasedEpochRandomGenerator generator = Generators.timeBasedEpochRandomGenerator();

    public UUID generateUuidV7() {
        return generator.generate();
    }
}
