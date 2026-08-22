package com.financeia.financeia_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "historial_analisis")
@Getter
@Setter
@NoArgsConstructor
public class HistorialAnalisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private Instant fechaHora;

    @Column(name = "perfil_financiero", nullable = false, length = 100)
    private String perfilFinanciero;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal probabilidad;

    @Column(name = "ingreso_mensual", nullable = false, precision = 19, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(name = "total_gastos", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalGastos;

    @Column(name = "ahorro_estimado", nullable = false, precision = 19, scale = 2)
    private BigDecimal ahorroEstimado;

    @Column(name = "nivel_endeudamiento", nullable = false, precision = 10, scale = 2)
    private BigDecimal nivelEndeudamiento;

    @Column(name = "frecuencia_ahorro", nullable = false, length = 50)
    private String frecuenciaAhorro;

    @Column(name = "resumen_gastos", columnDefinition = "TEXT")
    private String resumenGastos;

    @Column(columnDefinition = "TEXT")
    private String recomendaciones;
}
