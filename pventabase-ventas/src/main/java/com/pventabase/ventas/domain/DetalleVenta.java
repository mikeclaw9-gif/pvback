package com.pventabase.ventas.domain;

import com.pventabase.common.entity.BaseEntity;
import com.pventabase.inventario.entity.Producto;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "detalle_venta")
@Getter
@Setter
public class DetalleVenta extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull
    @DecimalMin("0.001")
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "precio_unitario", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioUnitario;

    @Min(0)
    @Column(precision = 5, scale = 2)
    private Integer descuentoPorcentaje;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        BigDecimal base = cantidad.multiply(precioUnitario);
        if (descuentoPorcentaje != null && descuentoPorcentaje > 0) {
            BigDecimal descuento = base.multiply(BigDecimal.valueOf(descuentoPorcentaje))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            subtotal = base.subtract(descuento);
        } else {
            subtotal = base;
        }
    }
}
