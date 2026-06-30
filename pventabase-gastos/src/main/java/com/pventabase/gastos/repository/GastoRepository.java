package com.pventabase.gastos.repository;

import com.pventabase.gastos.entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    @Query("SELECT COALESCE(SUM(g.monto), 0) FROM Gasto g WHERE g.fechaGasto BETWEEN :inicio AND :fin")
    BigDecimal sumMontoEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    List<Gasto> findByCategoria(String categoria);

    List<Gasto> findByFechaGastoBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Gasto> findByCategoriaAndFechaGastoBetween(String categoria, LocalDateTime inicio, LocalDateTime fin);
}
