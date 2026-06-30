package com.pventabase.ventas.repository;

import com.pventabase.clientes.entity.Cliente;
import com.pventabase.ventas.domain.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumTotalCompletadasEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT v.metodoPago, COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fecha BETWEEN :inicio AND :fin GROUP BY v.metodoPago")
    List<Object[]> sumTotalGroupedByMetodoPagoEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    Page<Venta> findByCliente(Cliente cliente, Pageable pageable);

    Page<Venta> findByEstado(Venta.EstadoVenta estado, Pageable pageable);

    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin")
    Page<Venta> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);

    @Query("SELECT v FROM Venta v WHERE v.cliente = :cliente AND v.fecha BETWEEN :inicio AND :fin")
    Page<Venta> findByClienteAndFechaBetween(@Param("cliente") Cliente cliente, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);

    @Query("SELECT v FROM Venta v WHERE v.estado = :estado AND v.fecha BETWEEN :inicio AND :fin")
    Page<Venta> findByEstadoAndFechaBetween(@Param("estado") Venta.EstadoVenta estado, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, Pageable pageable);
}
