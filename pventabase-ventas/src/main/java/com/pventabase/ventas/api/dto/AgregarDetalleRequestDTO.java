package com.pventabase.ventas.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AgregarDetalleRequestDTO {

    @NotNull
    private Long productoId;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal precioUnitario;

    @Min(0)
    private Integer descuentoPorcentaje;
}
