package com.pventabase.reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pventabase.reportes.dto.ReporteData;
import com.pventabase.reportes.dto.ReporteFilter;
import com.pventabase.reportes.enums.ReportFormat;
import com.pventabase.reportes.service.ReporteService;
import com.pventabase.reportes.util.ExcelGenerator;
import com.pventabase.reportes.util.PdfGenerator;
import com.pventabase.reportes.util.PrintServiceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ExcelGenerator excelGenerator;
    private final PdfGenerator pdfGenerator;
    private final PrintServiceUtil printServiceUtil;
    private final ObjectMapper objectMapper;

    private static final String NOMBRE_PDF = "reporte-pventabase.pdf";
    private static final String NOMBRE_EXCEL = "reporte-pventabase.xlsx";

    @GetMapping("/ventas")
    public ResponseEntity<?> reporteVentas(@RequestParam(required = false) String filter,
                                            @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteVentas(filtro);
        return exportarSegunFormato(data, formato, "reporte-ventas");
    }

    @GetMapping("/productos")
    public ResponseEntity<?> reporteProductos(@RequestParam(required = false) String filter,
                                               @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteProductosMasVendidos(filtro);
        return exportarSegunFormato(data, formato, "reporte-productos");
    }

    @GetMapping("/gastos")
    public ResponseEntity<?> reporteGastos(@RequestParam(required = false) String filter,
                                            @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteGastos(filtro);
        return exportarSegunFormato(data, formato, "reporte-gastos");
    }

    @GetMapping("/stock")
    public ResponseEntity<?> reporteStock(@RequestParam(required = false) String filter,
                                           @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteStock(filtro);
        return exportarSegunFormato(data, formato, "reporte-stock");
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> reporteClientes(@RequestParam(required = false) String filter,
                                              @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteClientesFrecuentes(filtro);
        return exportarSegunFormato(data, formato, "reporte-clientes");
    }

    @GetMapping("/cortes-caja")
    public ResponseEntity<?> reporteCortes(@RequestParam(required = false) String filter,
                                            @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarReporteCortesCaja(filtro);
        return exportarSegunFormato(data, formato, "reporte-cortes-caja");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestParam(required = false) String filter,
                                        @RequestParam(defaultValue = "JSON") ReportFormat formato) {
        ReporteFilter filtro = parseFilter(filter);
        ReporteData data = reporteService.generarDashboard(filtro);
        return exportarSegunFormato(data, formato, "dashboard-diario");
    }

    private ReporteFilter parseFilter(String filter) {
        if (filter == null || filter.isBlank() || filter.equals("{}")) {
            return new ReporteFilter();
        }
        try {
            Map<String, Object> raw = objectMapper.readValue(filter,
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            ReporteFilter f = new ReporteFilter();
            f.setFechaDesde(asString(raw.get("fechaDesde")));
            f.setFechaHasta(asString(raw.get("fechaHasta")));
            f.setEstado(asString(raw.get("estado")));
            f.setMetodoPago(asString(raw.get("metodoPago")));
            f.setCategoria(asString(raw.get("categoria")));
            f.setUsuarioEmail(asString(raw.get("usuarioEmail")));
            f.setFecha(asString(raw.get("fecha")));
            if (raw.get("clienteId") != null) f.setClienteId(Long.valueOf(asString(raw.get("clienteId"))));
            if (raw.get("limite") != null) f.setLimite(Integer.valueOf(asString(raw.get("limite"))));
            if (raw.get("stockMinimo") != null) f.setStockMinimo(Integer.valueOf(asString(raw.get("stockMinimo"))));
            if (raw.get("soloActivos") != null) f.setSoloActivos(Boolean.valueOf(asString(raw.get("soloActivos"))));
            return f;
        } catch (Exception e) {
            return new ReporteFilter();
        }
    }

    @SuppressWarnings("unchecked")
    private String asString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            Object v = ((Map<String, Object>) value).get("value");
            return v != null ? v.toString() : null;
        }
        return value.toString();
    }

    private ResponseEntity<?> exportarSegunFormato(ReporteData data, ReportFormat formato, String nombreBase) {
        try {
            return switch (formato) {
                case JSON -> ResponseEntity.ok(data);
                case EXCEL -> {
                    byte[] excel = excelGenerator.generarExcel(data);
                    yield ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreBase + "-" + NOMBRE_EXCEL)
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                            .body(excel);
                }
                case PDF -> {
                    byte[] pdf = pdfGenerator.generarPdf(data);
                    yield ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreBase + "-" + NOMBRE_PDF)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(pdf);
                }
                case PRINT -> {
                    byte[] pdf = pdfGenerator.generarPdf(data);
                    printServiceUtil.imprimirPdf(pdf, nombreBase);
                    yield ResponseEntity.ok("Reporte enviado a la impresora: " + nombreBase);
                }
            };
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al generar el reporte: " + e.getMessage());
        }
    }
}
