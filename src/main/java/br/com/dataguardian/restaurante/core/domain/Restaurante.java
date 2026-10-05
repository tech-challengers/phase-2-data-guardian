package br.com.dataguardian.restaurante.core.domain;

public class Restaurante {

    private Long id;
    private String nome;
    private String endereco;
    private String gastronomia;
    private String horarioFuncionamento;
    private Long donoId;

    public Restaurante(){}

    // Restaurante novo
    public Restaurante(String nome, String endereco, String gastronomia, String horarioFuncionamento, Long donoId) {
        this.nome = nome;
        this.endereco = endereco;
        this.gastronomia = gastronomia;
        this.horarioFuncionamento = horarioFuncionamento;
        this.donoId = donoId;
    }

    public Restaurante(Long id, String nome, String endereco, String gastronomia, String horarioFuncionamento, Long donoId) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.gastronomia = gastronomia;
        this.horarioFuncionamento = horarioFuncionamento;
        this.donoId = donoId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getGastronomia() {
        return gastronomia;
    }

    public void setGastronomia(String gastronomia) {
        this.gastronomia = gastronomia;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public Long getDonoId() {
        return donoId;
    }

    public void setDonoId(Long donoId) {
        this.donoId = donoId;
    }
}
