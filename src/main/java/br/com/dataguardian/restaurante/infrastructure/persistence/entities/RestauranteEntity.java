package br.com.dataguardian.restaurante.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurante")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestauranteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "endereco", nullable = false)
    private String endereco;

    @Column(name = "gastronomia", nullable = false)
    private String gastronomia;

    @Column(name = "horario_funcionamento", nullable = false)
    private String horarioFuncionamento;

    @Column(name = "dono_id")
    private Long donoId;
}
