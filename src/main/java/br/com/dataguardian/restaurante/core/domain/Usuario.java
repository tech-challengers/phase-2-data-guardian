package br.com.dataguardian.restaurante.core.domain;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario tipoUsuario;

    public Usuario(String nome, String email, String senha) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
        }
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public void associarTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo de usuário não pode ser nulo.");
        }
        this.tipoUsuario = tipo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }

    public void setId(Long id) { this.id = id; }
}
