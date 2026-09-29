package br.com.restaurante.core.services;

import br.com.restaurante.application.ports.in.UserTypeUseCase;
import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.domain.UserType;
import java.util.List;
import java.util.Optional;

public class UserTypeService implements UserTypeUseCase {

    private final UserTypeRepositoryPort repositoryPort;

    public UserTypeService(UserTypeRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public UserType create(UserType userType) {
        if (userType.getName() == null || userType.getName().trim().isEmpty()) {
             throw new IllegalArgumentException("Name cannot be empty");
        }
        if (repositoryPort.existsByName(userType.getName())) {
            throw new IllegalArgumentException("UserType with this name already exists");
        }
        return repositoryPort.save(userType);
    }

    @Override
    public Optional<UserType> findById(Long id) {
        return repositoryPort.findById(id);
    }

    @Override
    public List<UserType> findAll() {
        return repositoryPort.findAll();
    }

    @Override
    public UserType update(Long id, UserType userTypeDetails) {
        if (userTypeDetails.getName() == null || userTypeDetails.getName().trim().isEmpty()) {
             throw new IllegalArgumentException("Name cannot be empty");
        }
        UserType existing = repositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserType not found"));
        existing.setName(userTypeDetails.getName());
        return repositoryPort.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (repositoryPort.findById(id).isEmpty()) {
            throw new IllegalArgumentException("UserType not found");
        }
        repositoryPort.deleteById(id);
    }
}