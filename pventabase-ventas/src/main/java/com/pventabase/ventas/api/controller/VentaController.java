package com.pventabase.ventas.api.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.ventas.api.dto.AgregarDetalleRequestDTO;
import com.pventabase.ventas.api.dto.TicketResponseDTO;
import com.pventabase.ventas.api.dto.VentaRequestDTO;
import com.pventabase.ventas.api.dto.VentaResponseDTO;
import com.pventabase.ventas.domain.Venta;
import com.pventabase.ventas.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<VentaResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir,
            @RequestParam(required = false) Venta.EstadoVenta estado,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta) {
        if (estado != null || clienteId != null || fechaDesde != null || fechaHasta != null) {
            return ResponseEntity.ok(ventaService.findByFilters(estado, clienteId, fechaDesde, fechaHasta, page, size, sortBy, sortDir));
        }
        return ResponseEntity.ok(ventaService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.findById(id));
    }

    @GetMapping("/{id}/ticket")
    public ResponseEntity<TicketResponseDTO> ticket(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.generarTicket(id));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> create(@Valid @RequestBody VentaRequestDTO requestDTO,
                                                    Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.create(requestDTO, email));
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<VentaResponseDTO> agregarDetalle(@PathVariable Long id,
                                                            @Valid @RequestBody AgregarDetalleRequestDTO detalleDTO) {
        return ResponseEntity.ok(ventaService.agregarDetalle(id, detalleDTO));
    }

    @DeleteMapping("/{id}/detalles/{detalleId}")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Long id, @PathVariable Long detalleId) {
        ventaService.eliminarDetalle(id, detalleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/finalizar")
    public ResponseEntity<VentaResponseDTO> finalizarVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.finalizarVenta(id));
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anularVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.anularVenta(id));
    }
}
