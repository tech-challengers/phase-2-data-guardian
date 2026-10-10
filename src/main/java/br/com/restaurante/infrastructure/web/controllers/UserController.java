package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.core.domain.User;
import br.com.restaurante.infrastructure.web.dto.AssignUserTypeRequest;
import br.com.restaurante.infrastructure.web.dto.UserRequest;
import br.com.restaurante.infrastructure.web.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController implements UserApi {

    private final UserUseCase useCase;

    public UserController(UserUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        User domain = new User(null, request.getName(), request.getEmail(), null);
        User created = useCase.create(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Override
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = useCase.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = useCase.findById(id);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        User domain = new User(id, request.getName(), request.getEmail(), null);
        User updated = useCase.update(id, domain);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Override
    public ResponseEntity<UserResponse> assignUserType(Long id, AssignUserTypeRequest request) {
        User updated = useCase.assignUserType(id, request.getUserTypeId());
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
    private UserResponse toResponse(User domain) {
        UserResponse req = new UserResponse();
        req.setId(domain.getId());
        req.setName(domain.getName());
        req.setEmail(domain.getEmail());
        if (domain.getUserType() != null) {
            br.com.restaurante.infrastructure.web.dto.UserType dt = new br.com.restaurante.infrastructure.web.dto.UserType();
            dt.setId(domain.getUserType().getId());
            dt.setName(domain.getUserType().getName());
            req.setUserType(dt);
        }
        return req;
    }
}
