package com.pventabase.gastos.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class GastoRequestDTO {

    @NotBlank
    @Size(max = 255)
    private String descripcion;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal monto;

    @NotNull
    private LocalDateTime fechaGasto;

    @NotBlank
    @Size(max = 50)
    private String categoria;

    @Size(max = 50)
    private String metodoPago;

    @Size(max = 500)
    private String observacion;
}
