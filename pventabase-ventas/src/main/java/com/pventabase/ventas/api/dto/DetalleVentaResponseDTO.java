package com.pventabase.ventas.api.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DetalleVentaResponseDTO extends BaseResponseDTO {

    private Long productoId;
    private String productoCodigo;
    private String productoNombre;
    private String productoDescripcion;
    private Boolean productoPesado;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private Integer descuentoPorcentaje;
    private BigDecimal subtotal;
}
