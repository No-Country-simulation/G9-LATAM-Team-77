package com.financeia.financeia_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "historial_analisis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "ingreso_mensual")
    private BigDecimal ingresoMensual;

    @Column(name = "nivel_endeudamiento")
    private BigDecimal nivelEndeudamiento;

    @Column(name = "frecuencia_ahorro")
    private String frecuenciaAhorro;

    @Column(name = "total_gastos")
    private BigDecimal totalGastos;

    @Column(name = "ahorro_estimado")
    private BigDecimal ahorroEstimado;

    @Column(name = "score_financiero")
    private String scoreFinanciero;

    @Column(name = "resumen_categorias", columnDefinition = "TEXT")
    private String resumenCategorias;

}
