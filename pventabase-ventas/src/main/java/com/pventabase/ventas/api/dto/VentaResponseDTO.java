package com.pventabase.ventas.api.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import com.pventabase.ventas.domain.Venta;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VentaResponseDTO extends BaseResponseDTO {

    private LocalDateTime fecha;
    private BigDecimal total;
    private Integer descuentoPorcentaje;
    private Venta.EstadoVenta estado;
    private Long clienteId;
    private String clienteNombre;
    private String usuarioEmail;
    private List<DetalleVentaResponseDTO> detalles = new ArrayList<>();

    public List<DetalleVentaResponseDTO> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public void setDetalles(List<DetalleVentaResponseDTO> detalles) {
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
    }
}
