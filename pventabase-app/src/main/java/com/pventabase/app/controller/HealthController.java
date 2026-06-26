package com.pventabase.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(3)) {
                return ResponseEntity.ok(Map.of("disponible", true));
            }
            return ResponseEntity.ok(Map.of(
                    "disponible", false,
                    "mensaje", "La base de datos no responde"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "disponible", false,
                    "mensaje", "Error de conexion: " + e.getMessage()
            ));
        }
    }
}
