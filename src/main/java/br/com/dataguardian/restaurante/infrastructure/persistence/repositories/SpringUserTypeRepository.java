package br.com.dataguardian.restaurante.infrastructure.persistence.repositories;

import br.com.dataguardian.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringUserTypeRepository extends JpaRepository<UserTypeEntity, Long> {
    boolean existsByName(String name);
}
