package br.com.restaurante.application.ports.in;

import br.com.restaurante.core.domain.User;
import java.util.List;

public interface UserUseCase {
    User create(User user);
    List<User> findAll();
    User assignUserType(Long userId, Long userTypeId);
}
