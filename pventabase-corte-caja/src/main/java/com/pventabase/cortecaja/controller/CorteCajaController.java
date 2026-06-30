package com.pventabase.cortecaja.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.cortecaja.dto.AbrirCorteRequestDTO;
import com.pventabase.cortecaja.dto.CerrarCorteRequestDTO;
import com.pventabase.cortecaja.dto.CorteCajaResponseDTO;
import com.pventabase.cortecaja.service.CorteCajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cortes-caja")
@RequiredArgsConstructor
public class CorteCajaController {

    private final CorteCajaService corteCajaService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<CorteCajaResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        return ResponseEntity.ok(corteCajaService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorteCajaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(corteCajaService.findById(id));
    }

    @GetMapping("/abierto")
    public ResponseEntity<CorteCajaResponseDTO> findCorteAbierto() {
        return ResponseEntity.ok(corteCajaService.findCorteAbierto());
    }

    @PostMapping("/abrir")
    public ResponseEntity<CorteCajaResponseDTO> abrirCorte(@Valid @RequestBody AbrirCorteRequestDTO requestDTO,
                                                            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(corteCajaService.abrirCorte(requestDTO, email));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<CorteCajaResponseDTO> cerrarCorte(@PathVariable Long id,
                                                             @Valid @RequestBody CerrarCorteRequestDTO requestDTO) {
        return ResponseEntity.ok(corteCajaService.cerrarCorte(id, requestDTO));
    }
}
