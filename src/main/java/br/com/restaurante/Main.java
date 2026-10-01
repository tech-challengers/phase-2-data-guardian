package br.com.restaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"br.com.dataguardian.restaurante", "br.com.restaurante"})
@EntityScan(basePackages = {"br.com.restaurante.infrastructure.persistence.entities"})
@EnableJpaRepositories(basePackages = {"br.com.restaurante.infrastructure.persistence.repositories"})
public class Main {
    static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
