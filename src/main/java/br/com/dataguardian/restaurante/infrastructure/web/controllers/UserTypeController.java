package br.com.dataguardian.restaurante.infrastructure.web.controllers;

import br.com.dataguardian.restaurante.application.ports.in.UserTypeUseCase;
import br.com.dataguardian.restaurante.infrastructure.web.dto.UserType;
import br.com.dataguardian.restaurante.infrastructure.web.dto.UserTypeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserTypeController implements UserTypeApi {

    private final UserTypeUseCase useCase;

    public UserTypeController(UserTypeUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<UserType> createUserType(UserTypeRequest request) {
        br.com.dataguardian.restaurante.core.domain.UserType domain = new br.com.dataguardian.restaurante.core.domain.UserType(null, request.getName());
        br.com.dataguardian.restaurante.core.domain.UserType created = useCase.create(domain);

        UserType response = new UserType();
        response.setId(created.getId());
        response.setName(created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<Void> deleteUserType(Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UserType> getUserTypeById(Long id) {
        return useCase.findById(id).map(domain -> {
            UserType response = new UserType();
            response.setId(domain.getId());
            response.setName(domain.getName());
            return ResponseEntity.ok(response);
        }).orElseThrow(() -> new IllegalArgumentException("UserType not found"));
    }

    @Override
    public ResponseEntity<List<UserType>> listUserTypes() {
        List<UserType> types = useCase.findAll().stream().map(domain -> {
            UserType response = new UserType();
            response.setId(domain.getId());
            response.setName(domain.getName());
            return response;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @Override
    public ResponseEntity<UserType> updateUserType(Long id, UserTypeRequest request) {
        br.com.dataguardian.restaurante.core.domain.UserType domain = new br.com.dataguardian.restaurante.core.domain.UserType(id, request.getName());
        br.com.dataguardian.restaurante.core.domain.UserType updated = useCase.update(id, domain);

        UserType response = new UserType();
        response.setId(updated.getId());
        response.setName(updated.getName());
        return ResponseEntity.ok(response);
    }
}
