package com.financeia.financeia_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Moneda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String simbolo;
}