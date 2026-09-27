package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.UserType;
import java.util.List;
import java.util.Optional;

public interface UserTypeUseCase {
    UserType create(UserType userType);
    Optional<UserType> findById(Long id);
    List<UserType> findAll();
    UserType update(Long id, UserType userType);
    void delete(Long id);
}
