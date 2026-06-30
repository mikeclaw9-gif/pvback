package com.pventabase.reportes.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReporteFilter {

    private String fechaDesde;
    private String fechaHasta;
    private String estado;
    private String metodoPago;
    private String categoria;
    private String usuarioEmail;
    private Long clienteId;
    private Integer limite;
    private Integer stockMinimo;
    private Boolean soloActivos;
    private String fecha;
}
