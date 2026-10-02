package br.com.dataguardian.restaurante.application.ports.out;

import br.com.dataguardian.restaurante.core.domain.User;
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