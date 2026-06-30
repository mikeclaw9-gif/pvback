package com.pventabase.reportes.service;

import com.pventabase.clientes.entity.Cliente;
import com.pventabase.cortecaja.entity.CorteCaja;
import com.pventabase.cortecaja.repository.CorteCajaRepository;
import com.pventabase.gastos.repository.GastoRepository;
import com.pventabase.inventario.entity.Producto;
import com.pventabase.inventario.repository.ProductoRepository;
import com.pventabase.reportes.dto.ReporteData;
import com.pventabase.reportes.dto.ReporteFilter;
import com.pventabase.reportes.enums.TipoGrafico;
import com.pventabase.reportes.util.ChartGenerator;
import com.pventabase.ventas.domain.Venta;
import com.pventabase.ventas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;
    private final ProductoRepository productoRepository;
    private final CorteCajaRepository corteCajaRepository;
    private final ChartGenerator chartGenerator;

    private LocalDateTime parseFechaDesde(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return LocalDate.now().withDayOfMonth(1).atStartOfDay();
        }
        return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE).atStartOfDay();
    }

    private LocalDateTime parseFechaHasta(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return LocalDateTime.now();
        }
        return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE).atTime(LocalTime.MAX);
    }

    private LocalDate parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE);
    }

    public ReporteData generarReporteVentas(ReporteFilter filter) {
        LocalDateTime desde = parseFechaDesde(filter.getFechaDesde());
        LocalDateTime hasta = parseFechaHasta(filter.getFechaHasta());

        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (filter.getEstado() != null && !filter.getEstado().isEmpty()) {
            ventas = ventas.stream()
                    .filter(v -> v.getEstado().name().equalsIgnoreCase(filter.getEstado()))
                    .collect(Collectors.toList());
        }
        if (filter.getMetodoPago() != null && !filter.getMetodoPago().isEmpty()) {
            ventas = ventas.stream()
                    .filter(v -> filter.getMetodoPago().equalsIgnoreCase(v.getMetodoPago()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> filas = ventas.stream().map(v -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ID", v.getId());
            row.put("Fecha", v.getFecha());
            row.put("Cliente", v.getCliente() != null ? v.getCliente().getNombre() + " " + v.getCliente().getApellido() : "Mostrador");
            row.put("Usuario", v.getUsuario() != null ? v.getUsuario().getEmail() : "");
            row.put("Estado", v.getEstado().name());
            row.put("Total", v.getTotal());
            row.put("MetodoPago", v.getMetodoPago() != null ? v.getMetodoPago() : "");
            return row;
        }).collect(Collectors.toList());

        Map<LocalDate, BigDecimal> ventasPorDia = ventas.stream()
                .filter(v -> v.getEstado() == Venta.EstadoVenta.COMPLETADA)
                .collect(Collectors.groupingBy(
                        v -> v.getFecha().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Venta::getTotal, BigDecimal::add)
                ));

        List<String> dias = new ArrayList<>(ventasPorDia.keySet()).stream()
                .sorted().map(LocalDate::toString).collect(Collectors.toList());
        List<Double> valores = dias.stream()
                .map(d -> ventasPorDia.get(LocalDate.parse(d)).doubleValue())
                .collect(Collectors.toList());

        ReporteData.ReporteDataBuilder builder = ReporteData.builder()
                .titulo("Reporte de Ventas")
                .columnas(List.of("ID", "Fecha", "Cliente", "Usuario", "Estado", "Total", "MetodoPago"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.LINEA)
                .graficoNombre("Ventas por dia");

        if (!dias.isEmpty()) {
            builder.graficoNombre("Ventas por dia");
        }

        return builder.build();
    }

    public ReporteData generarReporteProductosMasVendidos(ReporteFilter filter) {
        LocalDateTime desde = parseFechaDesde(filter.getFechaDesde());
        LocalDateTime hasta = parseFechaHasta(filter.getFechaHasta());
        int limite = filter.getLimite() != null ? filter.getLimite() : 10;

        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        Map<Long, Map<String, Object>> productoMap = new LinkedHashMap<>();
        for (Venta v : ventas) {
            if (v.getEstado() != Venta.EstadoVenta.COMPLETADA) continue;
            for (var detalle : v.getDetalles()) {
                Producto p = detalle.getProducto();
                productoMap.computeIfAbsent(p.getId(), k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("Producto", p.getNombre());
                    m.put("Codigo", p.getCodigo());
                    m.put("Cantidad", BigDecimal.ZERO);
                    m.put("Total", BigDecimal.ZERO);
                    return m;
                });
                Map<String, Object> row = productoMap.get(p.getId());
                row.put("Cantidad", ((BigDecimal) row.get("Cantidad")).add(detalle.getCantidad()));
                row.put("Total", ((BigDecimal) row.get("Total")).add(detalle.getSubtotal()));
            }
        }

        List<Map<String, Object>> filas = productoMap.values().stream()
                .sorted((a, b) -> ((BigDecimal) b.get("Cantidad")).compareTo((BigDecimal) a.get("Cantidad")))
                .limit(limite)
                .collect(Collectors.toList());

        return ReporteData.builder()
                .titulo("Productos mas vendidos")
                .columnas(List.of("Producto", "Codigo", "Cantidad", "Total"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.BARRA)
                .graficoNombre("Top productos")
                .build();
    }

    public ReporteData generarReporteGastos(ReporteFilter filter) {
        LocalDateTime desde = parseFechaDesde(filter.getFechaDesde());
        LocalDateTime hasta = parseFechaHasta(filter.getFechaHasta());

        List<Map<String, Object>> filas;
        if (filter.getCategoria() != null && !filter.getCategoria().isEmpty()) {
            filas = gastoRepository.findByCategoriaAndFechaGastoBetween(filter.getCategoria(), desde, hasta)
                    .stream().map(g -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("ID", g.getId());
                        row.put("Fecha", g.getFechaGasto());
                        row.put("Descripcion", g.getDescripcion());
                        row.put("Categoria", g.getCategoria());
                        row.put("Monto", g.getMonto());
                        row.put("MetodoPago", g.getMetodoPago() != null ? g.getMetodoPago() : "");
                        return row;
                    }).collect(Collectors.toList());
        } else {
            filas = gastoRepository.findByFechaGastoBetween(desde, hasta)
                    .stream().map(g -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("ID", g.getId());
                        row.put("Fecha", g.getFechaGasto());
                        row.put("Descripcion", g.getDescripcion());
                        row.put("Categoria", g.getCategoria());
                        row.put("Monto", g.getMonto());
                        row.put("MetodoPago", g.getMetodoPago() != null ? g.getMetodoPago() : "");
                        return row;
                    }).collect(Collectors.toList());
        }

        return ReporteData.builder()
                .titulo("Reporte de Gastos")
                .columnas(List.of("ID", "Fecha", "Descripcion", "Categoria", "Monto", "MetodoPago"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.PIE)
                .graficoNombre("Gastos por categoria")
                .build();
    }

    public ReporteData generarReporteStock(ReporteFilter filter) {
        int stockMinimo = filter.getStockMinimo() != null ? filter.getStockMinimo() : 0;
        boolean soloActivos = filter.getSoloActivos() != null ? filter.getSoloActivos() : true;

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> !soloActivos || p.getActivo())
                .filter(p -> stockMinimo <= 0 || p.getExistencia() <= stockMinimo)
                .sorted(Comparator.comparingInt(Producto::getExistencia))
                .collect(Collectors.toList());

        List<Map<String, Object>> filas = productos.stream().map(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Producto", p.getNombre());
            row.put("Codigo", p.getCodigo());
            row.put("Existencia", p.getExistencia());
            row.put("PrecioVenta", p.getPrecioVenta());
            row.put("Activo", p.getActivo());
            return row;
        }).collect(Collectors.toList());

        return ReporteData.builder()
                .titulo("Reporte de Inventario / Stock")
                .columnas(List.of("Producto", "Codigo", "Existencia", "PrecioVenta", "Activo"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.NONE)
                .build();
    }

    public ReporteData generarReporteClientesFrecuentes(ReporteFilter filter) {
        LocalDateTime desde = parseFechaDesde(filter.getFechaDesde());
        LocalDateTime hasta = parseFechaHasta(filter.getFechaHasta());
        int limite = filter.getLimite() != null ? filter.getLimite() : 10;

        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        Map<Long, Map<String, Object>> clienteMap = new LinkedHashMap<>();
        for (Venta v : ventas) {
            if (v.getEstado() != Venta.EstadoVenta.COMPLETADA || v.getCliente() == null) continue;
            Cliente c = v.getCliente();
            clienteMap.computeIfAbsent(c.getId(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("Cliente", c.getNombre() + " " + c.getApellido());
                m.put("Email", c.getEmail());
                m.put("Compras", 0L);
                m.put("TotalGastado", BigDecimal.ZERO);
                return m;
            });
            Map<String, Object> row = clienteMap.get(c.getId());
            row.put("Compras", ((Long) row.get("Compras")) + 1);
            row.put("TotalGastado", ((BigDecimal) row.get("TotalGastado")).add(v.getTotal()));
        }

        List<Map<String, Object>> filas = clienteMap.values().stream()
                .sorted((a, b) -> ((BigDecimal) b.get("TotalGastado")).compareTo((BigDecimal) a.get("TotalGastado")))
                .limit(limite)
                .collect(Collectors.toList());

        return ReporteData.builder()
                .titulo("Clientes frecuentes")
                .columnas(List.of("Cliente", "Email", "Compras", "TotalGastado"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.BARRA)
                .graficoNombre("Top clientes")
                .build();
    }

    public ReporteData generarReporteCortesCaja(ReporteFilter filter) {
        LocalDateTime desde = parseFechaDesde(filter.getFechaDesde());
        LocalDateTime hasta = parseFechaHasta(filter.getFechaHasta());

        List<CorteCaja> cortes = corteCajaRepository.findAll().stream()
                .filter(c -> c.getFechaApertura().isAfter(desde) && c.getFechaApertura().isBefore(hasta))
                .sorted(Comparator.comparing(CorteCaja::getFechaApertura).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> filas = cortes.stream().map(c -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ID", c.getId());
            row.put("Apertura", c.getFechaApertura());
            row.put("Cierre", c.getFechaCierre());
            row.put("MontoInicial", c.getMontoInicial());
            row.put("MontoFinal", c.getMontoFinal());
            row.put("TotalVentas", c.getTotalVentas());
            row.put("TotalGastos", c.getTotalGastos());
            row.put("Efectivo", c.getTotalEfectivo());
            row.put("Tarjeta", c.getTotalTarjeta());
            row.put("Transferencia", c.getTotalTransferencia());
            row.put("Diferencia", c.getDiferencia());
            row.put("Estado", c.getEstado().name());
            return row;
        }).collect(Collectors.toList());

        return ReporteData.builder()
                .titulo("Reporte de Cortes de Caja")
                .columnas(List.of("ID", "Apertura", "Cierre", "MontoInicial", "MontoFinal",
                        "TotalVentas", "TotalGastos", "Efectivo", "Tarjeta", "Transferencia", "Diferencia", "Estado"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.NONE)
                .build();
    }

    public ReporteData generarDashboard(ReporteFilter filter) {
        LocalDate fecha = parseFecha(filter.getFecha());
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(LocalTime.MAX);

        BigDecimal totalVentas = ventaRepository.sumTotalCompletadasEntreFechas(desde, hasta);
        BigDecimal totalGastos = gastoRepository.sumMontoEntreFechas(desde, hasta);

        List<Object[]> ventasPorMetodo = ventaRepository.sumTotalGroupedByMetodoPagoEntreFechas(desde, hasta);
        Map<String, BigDecimal> metodoMap = ventasPorMetodo.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<Venta> ventas = ventaRepository.findByFechaBetween(desde, hasta, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        Map<Long, Map<String, Object>> prodCount = new LinkedHashMap<>();
        for (Venta v : ventas) {
            if (v.getEstado() != Venta.EstadoVenta.COMPLETADA) continue;
            for (var detalle : v.getDetalles()) {
                Producto p = detalle.getProducto();
                prodCount.computeIfAbsent(p.getId(), k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("Producto", p.getNombre());
                    m.put("Cantidad", BigDecimal.ZERO);
                    m.put("Total", BigDecimal.ZERO);
                    return m;
                });
                Map<String, Object> row = prodCount.get(p.getId());
                row.put("Cantidad", ((BigDecimal) row.get("Cantidad")).add(detalle.getCantidad()));
                row.put("Total", ((BigDecimal) row.get("Total")).add(detalle.getSubtotal()));
            }
        }

        List<Map<String, Object>> topProductos = prodCount.values().stream()
                .sorted((a, b) -> ((BigDecimal) b.get("Cantidad")).compareTo((BigDecimal) a.get("Cantidad")))
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> metricas = new ArrayList<>();
        metricas.add(Map.of("Metrica", "Total Ventas", "Valor", totalVentas));
        metricas.add(Map.of("Metrica", "Total Gastos", "Valor", totalGastos));
        metricas.add(Map.of("Metrica", "Balance", "Valor", totalVentas.subtract(totalGastos)));
        metricas.add(Map.of("Metrica", "Efectivo", "Valor", metodoMap.getOrDefault("EFECTIVO", BigDecimal.ZERO)));
        metricas.add(Map.of("Metrica", "Tarjeta", "Valor",
                metodoMap.getOrDefault("TARJETA_DEBITO", BigDecimal.ZERO)
                        .add(metodoMap.getOrDefault("TARJETA_CREDITO", BigDecimal.ZERO))));
        metricas.add(Map.of("Metrica", "Transferencia", "Valor", metodoMap.getOrDefault("TRANSFERENCIA", BigDecimal.ZERO)));

        List<Map<String, Object>> filas = new ArrayList<>();
        filas.add(Map.of("Metricas", "--- RESUMEN DEL DIA ---"));
        for (var m : metricas) {
            filas.add(Map.of("Metricas", m.get("Metrica") + ": $" + m.get("Valor")));
        }
        filas.add(Map.of("Metricas", "--- TOP PRODUCTOS ---"));
        for (var p : topProductos) {
            filas.add(Map.of("Metricas", p.get("Producto") + " - Cant: " + p.get("Cantidad") + " - Total: $" + p.get("Total")));
        }

        return ReporteData.builder()
                .titulo("Dashboard Diario - " + fecha)
                .columnas(List.of("Metricas"))
                .filas(filas)
                .tipoGrafico(TipoGrafico.PIE)
                .graficoNombre("Ventas por metodo de pago")
                .build();
    }
}
