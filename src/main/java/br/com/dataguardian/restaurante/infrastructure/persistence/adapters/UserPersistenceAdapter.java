package br.com.dataguardian.restaurante.infrastructure.persistence.adapters;

import br.com.dataguardian.restaurante.application.ports.out.UserRepositoryPort;
import br.com.dataguardian.restaurante.core.domain.User;
import br.com.dataguardian.restaurante.core.domain.UserType;
import br.com.dataguardian.restaurante.infrastructure.persistence.entities.UserEntity;
import br.com.dataguardian.restaurante.infrastructure.persistence.entities.UserTypeEntity;
import br.com.dataguardian.restaurante.infrastructure.persistence.repositories.SpringUserRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringUserRepository repository;

    public UserPersistenceAdapter(SpringUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());

        if (user.getUserType() != null) {
            entity.setUserType(new UserTypeEntity(user.getUserType().getId(), user.getUserType().getName()));
        }

        UserEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private User toDomain(UserEntity entity) {
        UserType ut = null;
        if (entity.getUserType() != null) {
            ut = new UserType(entity.getUserType().getId(), entity.getUserType().getName());
        }
        return new User(entity.getId(), entity.getName(), entity.getEmail(), ut);
    }
}