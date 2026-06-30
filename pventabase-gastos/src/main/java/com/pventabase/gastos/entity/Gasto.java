package com.pventabase.gastos.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gastos")
@Getter
@Setter
public class Gasto extends BaseEntity {

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @NotNull
    @Column(name = "fecha_gasto", nullable = false)
    private LocalDateTime fechaGasto;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String categoria;

    @Size(max = 50)
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Size(max = 500)
    @Column(length = 500)
    private String observacion;
}
