package com.pventabase.inventario.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductoResponseDTO extends BaseResponseDTO {

    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer existencia;
}
