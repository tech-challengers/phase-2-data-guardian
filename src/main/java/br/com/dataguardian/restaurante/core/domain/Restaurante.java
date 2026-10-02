package br.com.dataguardian.restaurante.core.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Restaurante {

    private Long id;
    private String nome;
    private String endereco;
    private String gastronomia;
    private String horarioFuncionamento;
    private Long donoId;

}
