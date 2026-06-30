package com.pventabase.cortecaja.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AbrirCorteRequestDTO {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal montoInicial;

    private String observacion;
}
