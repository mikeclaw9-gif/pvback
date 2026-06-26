package com.pventabase.ventas.repository;

import com.pventabase.inventario.entity.Producto;
import com.pventabase.ventas.domain.DetalleVenta;
import com.pventabase.ventas.domain.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVenta(Venta venta);

    List<DetalleVenta> findByProducto(Producto producto);

    void deleteByVentaAndId(Venta venta, Long detalleId);
}
