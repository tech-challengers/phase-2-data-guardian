package br.com.restaurante.infrastructure.persistence.adapters;

import br.com.restaurante.application.ports.out.UserTypeRepositoryPort;
import br.com.restaurante.core.domain.UserType;
import br.com.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import br.com.restaurante.infrastructure.persistence.repositories.SpringUserTypeRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserTypePersistenceAdapter implements UserTypeRepositoryPort {

    private final SpringUserTypeRepository repository;

    public UserTypePersistenceAdapter(SpringUserTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserType save(UserType userType) {
        UserTypeEntity entity = new UserTypeEntity(userType.getId(), userType.getName());
        UserTypeEntity saved = repository.save(entity);
        return new UserType(saved.getId(), saved.getName());
    }

    @Override
    public Optional<UserType> findById(Long id) {
        return repository.findById(id)
                .map(e -> new UserType(e.getId(), e.getName()));
    }

    @Override
    public Optional<UserType> findByName(String name) {
        return repository.findByName(name)
                .map(e -> new UserType(e.getId(), e.getName()));
    }

    @Override
    public List<UserType> findAll() {
        return repository.findAll().stream()
                .map(e -> new UserType(e.getId(), e.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }
}
