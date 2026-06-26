package com.pventabase.ventas.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VentaRequestDTO {

    private Long clienteId;

    @Size(min = 1)
    private List<DetalleVentaRequestDTO> detalles = new ArrayList<>();

    private Integer descuentoPorcentaje;

    public List<DetalleVentaRequestDTO> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public void setDetalles(List<DetalleVentaRequestDTO> detalles) {
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
    }
}
