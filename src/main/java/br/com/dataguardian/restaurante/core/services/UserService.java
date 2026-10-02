package br.com.dataguardian.restaurante.core.services;

import br.com.dataguardian.restaurante.application.ports.in.UserUseCase;
import br.com.dataguardian.restaurante.application.ports.out.UserRepositoryPort;
import br.com.dataguardian.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.dataguardian.restaurante.core.domain.User;
import br.com.dataguardian.restaurante.core.domain.UserType;
import java.util.List;

public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserTypeRepositoryPort userTypeRepositoryPort;

    public UserService(UserRepositoryPort userRepositoryPort, UserTypeRepositoryPort userTypeRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.userTypeRepositoryPort = userTypeRepositoryPort;
    }

    @Override
    public User create(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        return userRepositoryPort.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    public User assignUserType(Long userId, Long userTypeId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserType userType = userTypeRepositoryPort.findById(userTypeId)
                .orElseThrow(() -> new IllegalArgumentException("UserType not found"));

        user.assignType(userType);
        return userRepositoryPort.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    public User update(Long id, User updatedUser) {
        User existingUser = findById(id);

        existingUser.setName(updatedUser.getName());

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepositoryPort.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("User with this email already exists");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        return userRepositoryPort.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        User existingUser = findById(id);
        userRepositoryPort.deleteById(existingUser.getId());
    }
}