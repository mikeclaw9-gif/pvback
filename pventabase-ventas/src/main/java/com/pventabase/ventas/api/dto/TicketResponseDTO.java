package com.pventabase.ventas.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TicketResponseDTO {

    private Long ventaId;
    private LocalDateTime fecha;
    private String atendidoPor;
    private String cliente;
    private List<LineaTicket> lineas;
    private BigDecimal subtotal;
    private Integer descuentoPorcentaje;
    private BigDecimal descuentoAplicado;
    private BigDecimal total;

    @Getter
    @Builder
    public static class LineaTicket {
        private String producto;
        private BigDecimal cantidad;
        private String unidad;
        private BigDecimal precioUnitario;
        private Integer descuentoPorcentaje;
        private BigDecimal importe;
    }
}
