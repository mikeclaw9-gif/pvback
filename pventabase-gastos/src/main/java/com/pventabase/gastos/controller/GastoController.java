package com.pventabase.gastos.controller;

import com.pventabase.common.constants.AppConstants;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.gastos.dto.GastoRequestDTO;
import com.pventabase.gastos.dto.GastoResponseDTO;
import com.pventabase.gastos.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<GastoResponseDTO>> findAll(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR) String sortDir) {
        return ResponseEntity.ok(gastoService.findAll(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(gastoService.findById(id));
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<GastoResponseDTO>> findByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(gastoService.findByCategoria(categoria));
    }

    @GetMapping("/rango-fechas")
    public ResponseEntity<List<GastoResponseDTO>> findByFechaBetween(
            @RequestParam LocalDateTime desde,
            @RequestParam LocalDateTime hasta) {
        return ResponseEntity.ok(gastoService.findByFechaBetween(desde, hasta));
    }

    @PostMapping
    public ResponseEntity<GastoResponseDTO> create(@Valid @RequestBody GastoRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoService.create(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody GastoRequestDTO requestDTO) {
        return ResponseEntity.ok(gastoService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gastoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<GastoResponseDTO> toggleActivo(@PathVariable Long id) {
        return ResponseEntity.ok(gastoService.toggleActivo(id));
    }
}
