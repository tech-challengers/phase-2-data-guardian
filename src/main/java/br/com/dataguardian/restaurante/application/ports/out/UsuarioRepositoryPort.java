package br.com.dataguardian.restaurante.application.ports.out;

import br.com.restaurante.core.domain.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
}