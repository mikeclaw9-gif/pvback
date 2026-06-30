package com.pventabase.gastos.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class GastoResponseDTO extends BaseResponseDTO {

    private String descripcion;
    private BigDecimal monto;
    private LocalDateTime fechaGasto;
    private String categoria;
    private String metodoPago;
    private String observacion;
}
