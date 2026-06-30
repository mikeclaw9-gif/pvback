package com.pventabase.cortecaja.repository;

import com.pventabase.cortecaja.entity.CorteCaja;
import com.pventabase.cortecaja.enums.EstadoCorte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorteCajaRepository extends JpaRepository<CorteCaja, Long> {

    Optional<CorteCaja> findTopByEstadoOrderByFechaAperturaDesc(EstadoCorte estado);

    boolean existsByEstado(EstadoCorte estado);
}
