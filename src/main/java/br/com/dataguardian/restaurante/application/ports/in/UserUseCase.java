package br.com.dataguardian.restaurante.application.ports.in;

import br.com.dataguardian.restaurante.core.domain.User;
import java.util.List;

public interface UserUseCase {
    User create(User user);
    List<User> findAll();
    User assignUserType(Long userId, Long userTypeId);
    User findById(Long id);
    User update(Long id, User user);
    void delete(Long id);
}