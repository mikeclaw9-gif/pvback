package com.pventabase.cortecaja.entity;

import com.pventabase.common.entity.BaseEntity;
import com.pventabase.cortecaja.enums.EstadoCorte;
import com.pventabase.usuarios.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cortes_caja")
@Getter
@Setter
public class CorteCaja extends BaseEntity {

    @NotNull
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "monto_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_final", precision = 19, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "total_ventas", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalVentas = BigDecimal.ZERO;

    @Column(name = "total_gastos", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalGastos = BigDecimal.ZERO;

    @Column(name = "total_efectivo", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalEfectivo = BigDecimal.ZERO;

    @Column(name = "total_tarjeta", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTarjeta = BigDecimal.ZERO;

    @Column(name = "total_transferencia", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalTransferencia = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Size(max = 500)
    @Column(length = 500)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCorte estado = EstadoCorte.ABIERTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
