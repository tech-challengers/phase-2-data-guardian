package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.UserType;
import java.util.List;
import java.util.Optional;

public interface UserTypeRepositoryPort {
    UserType save(UserType userType);
    Optional<UserType> findById(Long id);
    List<UserType> findAll();
    void deleteById(Long id);
    boolean existsByName(String name);
}