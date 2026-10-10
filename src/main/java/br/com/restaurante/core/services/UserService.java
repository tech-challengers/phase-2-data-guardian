package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.application.ports.out.PasswordEncoderPort;
import br.com.restaurante.application.ports.out.UserRepositoryPort;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.core.domain.UserType;
import java.util.List;

public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final UserTypeRepositoryPort userTypeRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public UserService(
            UserRepositoryPort userRepositoryPort,
            UserTypeRepositoryPort userTypeRepositoryPort,
            PasswordEncoderPort passwordEncoderPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.userTypeRepositoryPort = userTypeRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    public UserService(UserRepositoryPort userRepositoryPort, UserTypeRepositoryPort userTypeRepositoryPort) {
        this(userRepositoryPort, userTypeRepositoryPort, null);
    }

    @Override
    public User create(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        if (user.getPassword() != null && !user.getPassword().isBlank() && passwordEncoderPort != null) {
            user.setPassword(passwordEncoderPort.encode(user.getPassword()));
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
}
