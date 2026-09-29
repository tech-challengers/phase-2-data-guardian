package br.com.restaurante.infrastructure.web.controllers;

import br.com.restaurante.application.ports.in.UserUseCase;
import br.com.restaurante.infrastructure.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController implements UserApi {

    private final UserUseCase useCase;

    public UserController(UserUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(UserRequest request) {
        br.com.restaurante.core.domain.User domain = new br.com.restaurante.core.domain.User(null, request.getName(), request.getEmail(), null);
        br.com.restaurante.core.domain.User created = useCase.create(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @Override
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = useCase.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserResponse> assignUserType(Long id, AssignUserTypeRequest request) {
        br.com.restaurante.core.domain.User updated = useCase.assignUserType(id, request.getUserTypeId());
        return ResponseEntity.ok(toResponse(updated));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@org.springframework.web.bind.annotation.PathVariable Long id) {
        br.com.restaurante.core.domain.User user = useCase.findById(id);
        return ResponseEntity.ok(toResponse(user));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestBody UserRequest request) {

        br.com.restaurante.core.domain.User domain = new br.com.restaurante.core.domain.User(null, request.getName(), request.getEmail(), null);
        br.com.restaurante.core.domain.User updated = useCase.update(id, domain);
        return ResponseEntity.ok(toResponse(updated));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@org.springframework.web.bind.annotation.PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(br.com.restaurante.core.domain.User domain) {
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