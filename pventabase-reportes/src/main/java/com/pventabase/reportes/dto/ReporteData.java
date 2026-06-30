package com.pventabase.reportes.dto;

import com.pventabase.reportes.enums.TipoGrafico;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ReporteData {

    private String titulo;
    private List<String> columnas;
    private List<Map<String, Object>> filas;
    private TipoGrafico tipoGrafico;
    private String graficoNombre;
}
