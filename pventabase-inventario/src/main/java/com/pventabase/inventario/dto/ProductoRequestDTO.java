package com.pventabase.inventario.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductoRequestDTO {

    @NotBlank
    @Size(min = 1, max = 50)
    private String codigo;

    @NotBlank
    @Size(min = 1, max = 150)
    private String nombre;

    private String descripcion;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal precioCompra;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal precioVenta;

    @Min(0)
    private Integer existencia = 0;
}
