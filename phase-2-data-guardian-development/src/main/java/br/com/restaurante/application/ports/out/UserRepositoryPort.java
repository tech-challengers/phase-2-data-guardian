package br.com.restaurante.application.ports.out;

import br.com.restaurante.core.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    void deleteById(Long id);
}