package br.com.restaurante.infrastructure.persistence.repositories;

import br.com.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SpringUserTypeRepository extends JpaRepository<UserTypeEntity, Long> {
    boolean existsByName(String name);
    Optional<UserTypeEntity> findByName(String name);
}
