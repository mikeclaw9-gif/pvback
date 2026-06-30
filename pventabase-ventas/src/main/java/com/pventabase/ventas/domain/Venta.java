package com.pventabase.ventas.domain;

import com.pventabase.clientes.entity.Cliente;
import com.pventabase.common.entity.BaseEntity;
import com.pventabase.usuarios.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
@Getter
@Setter
public class Venta extends BaseEntity {

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(precision = 5, scale = 2)
    private Integer descuentoPorcentaje;

    @Size(max = 50)
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    public void removerDetalle(DetalleVenta detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }

    public enum EstadoVenta {
        PENDIENTE, COMPLETADA, CANCELADA
    }
}
