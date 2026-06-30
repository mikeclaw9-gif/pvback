package com.pventabase.cortecaja.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import com.pventabase.cortecaja.enums.EstadoCorte;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CorteCajaResponseDTO extends BaseResponseDTO {

    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private BigDecimal totalVentas;
    private BigDecimal totalGastos;
    private BigDecimal totalEfectivo;
    private BigDecimal totalTarjeta;
    private BigDecimal totalTransferencia;
    private BigDecimal diferencia;
    private String observacion;
    private EstadoCorte estado;
    private String usuarioEmail;
    private String usuarioNombre;
}
