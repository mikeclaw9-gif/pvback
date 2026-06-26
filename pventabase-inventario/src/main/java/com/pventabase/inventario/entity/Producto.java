package com.pventabase.inventario.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "fecha_creacion")),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "fecha_modificacion"))
})
public class Producto extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "precio_compra", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioCompra;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "precio_venta", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioVenta;

    @Min(0)
    @Column(nullable = false)
    private Integer existencia = 0;

    @Column(nullable = false)
    private Boolean pesado = false;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
}
