package br.com.dataguardian.restaurante.core.domain;

public class TipoUsuario {

    private Long id;
    private String nome;

    public TipoUsuario(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do tipo de usuário é obrigatório.");
        }
        // Validação extra para garantir apenas os tipos permitidos pelo escopo
        if (!nome.equalsIgnoreCase("Dono de Restaurante") && !nome.equalsIgnoreCase("Cliente")) {
            throw new IllegalArgumentException("O tipo deve ser 'Dono de Restaurante' ou 'Cliente'.");
        }
        this.nome = nome;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }

    public void setId(Long id) { this.id = id; }
}