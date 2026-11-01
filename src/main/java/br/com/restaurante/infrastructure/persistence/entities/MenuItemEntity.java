package br.com.restaurante.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_cardapio",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_item_restaurante_nome",
                columnNames = {"restaurante_id", "nome"}))
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "restaurante_id", nullable = false)
    private Long restaurantId;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "descricao")
    private String description;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "apenas_no_restaurante", nullable = false)
    private boolean dineInOnly;

    @Column(name = "foto_url")
    private String photoPath;
}