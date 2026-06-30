package com.pventabase.ventas.service;

import com.pventabase.clientes.entity.Cliente;
import com.pventabase.clientes.repository.ClienteRepository;
import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.inventario.entity.Producto;
import com.pventabase.inventario.repository.ProductoRepository;
import com.pventabase.usuarios.entity.Usuario;
import com.pventabase.usuarios.repository.UsuarioRepository;
import com.pventabase.ventas.api.dto.AgregarDetalleRequestDTO;
import com.pventabase.ventas.api.dto.DetalleVentaResponseDTO;
import com.pventabase.ventas.api.dto.TicketResponseDTO;
import com.pventabase.ventas.api.dto.VentaRequestDTO;
import com.pventabase.ventas.api.dto.VentaResponseDTO;
import com.pventabase.ventas.api.mapper.DetalleVentaMapper;
import com.pventabase.ventas.api.mapper.VentaMapper;
import com.pventabase.ventas.domain.DetalleVenta;
import com.pventabase.ventas.domain.Venta;
import com.pventabase.ventas.exception.StockInsuficienteException;
import com.pventabase.ventas.exception.VentaNoEncontradaException;
import com.pventabase.ventas.repository.DetalleVentaRepository;
import com.pventabase.ventas.repository.VentaRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @SuppressFBWarnings("EI_EXPOSE_REP2"))
@Transactional
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<VentaResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Venta> ventaPage = ventaRepository.findAll(pageable);
        return buildPageResponse(ventaPage);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<VentaResponseDTO> findByFilters(Venta.EstadoVenta estado, Long clienteId,
                                                           LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                           int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Venta> ventaPage;

        if (clienteId != null && fechaInicio != null && fechaFin != null) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
            ventaPage = ventaRepository.findByClienteAndFechaBetween(cliente, fechaInicio, fechaFin, pageable);
        } else if (estado != null && fechaInicio != null && fechaFin != null) {
            ventaPage = ventaRepository.findByEstadoAndFechaBetween(estado, fechaInicio, fechaFin, pageable);
        } else if (clienteId != null) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
            ventaPage = ventaRepository.findByCliente(cliente, pageable);
        } else if (estado != null) {
            ventaPage = ventaRepository.findByEstado(estado, pageable);
        } else if (fechaInicio != null && fechaFin != null) {
            ventaPage = ventaRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } else {
            ventaPage = ventaRepository.findAll(pageable);
        }

        return buildPageResponse(ventaPage);
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO findById(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNoEncontradaException(id));
        return ventaMapper.toResponseDTO(venta);
    }

    private void calcularTotal(Venta venta) {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : venta.getDetalles()) {
            total = total.add(d.getSubtotal());
        }
        if (venta.getDescuentoPorcentaje() != null && venta.getDescuentoPorcentaje() > 0) {
            BigDecimal descuento = total.multiply(BigDecimal.valueOf(venta.getDescuentoPorcentaje()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            total = total.subtract(descuento);
        }
        venta.setTotal(total);
    }

    public VentaResponseDTO create(VentaRequestDTO requestDTO, String usuarioEmail) {
        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", usuarioEmail));

        Cliente cliente = null;
        if (requestDTO.getClienteId() != null) {
            cliente = clienteRepository.findById(requestDTO.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", requestDTO.getClienteId()));
        }

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(Venta.EstadoVenta.PENDIENTE);
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setDescuentoPorcentaje(requestDTO.getDescuentoPorcentaje());

        List<DetalleVenta> detalles = new ArrayList<>();

        for (var detalleDTO : requestDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", detalleDTO.getProductoId()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
            detalle.setDescuentoPorcentaje(detalleDTO.getDescuentoPorcentaje());
            detalle.calcularSubtotal();

            detalles.add(detalle);
        }

        venta.setDetalles(detalles);
        calcularTotal(venta);

        venta = ventaRepository.save(venta);
        return ventaMapper.toResponseDTO(venta);
    }

    public VentaResponseDTO anularVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNoEncontradaException(id));

        if (venta.getEstado() == Venta.EstadoVenta.CANCELADA) {
            throw new VentaNoEncontradaException("La venta ya ha sido cancelada");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setExistencia(producto.getExistencia() + detalle.getCantidad().intValue());
            productoRepository.save(producto);
        }

        venta.setEstado(Venta.EstadoVenta.CANCELADA);
        venta = ventaRepository.save(venta);
        return ventaMapper.toResponseDTO(venta);
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO generarTicket(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNoEncontradaException(id));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta d : venta.getDetalles()) {
            subtotal = subtotal.add(d.getSubtotal());
        }

        BigDecimal descuentoAplicado = BigDecimal.ZERO;
        if (venta.getDescuentoPorcentaje() != null && venta.getDescuentoPorcentaje() > 0) {
            descuentoAplicado = subtotal.multiply(BigDecimal.valueOf(venta.getDescuentoPorcentaje()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        String clienteNombre = venta.getCliente() != null
                ? venta.getCliente().getNombre() + " " + venta.getCliente().getApellido()
                : "Mostrador";

        List<TicketResponseDTO.LineaTicket> lineas = venta.getDetalles().stream()
                .map(d -> TicketResponseDTO.LineaTicket.builder()
                        .producto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .unidad(d.getProducto().getPesado() ? "kg" : "pza")
                        .precioUnitario(d.getPrecioUnitario())
                        .descuentoPorcentaje(d.getDescuentoPorcentaje())
                        .importe(d.getSubtotal())
                        .build())
                .toList();

        return TicketResponseDTO.builder()
                .ventaId(venta.getId())
                .fecha(venta.getFecha())
                .atendidoPor(venta.getUsuario().getEmail())
                .cliente(clienteNombre)
                .lineas(lineas)
                .subtotal(subtotal)
                .descuentoPorcentaje(venta.getDescuentoPorcentaje())
                .descuentoAplicado(descuentoAplicado)
                .total(venta.getTotal())
                .build();
    }

    public VentaResponseDTO agregarDetalle(Long ventaId, AgregarDetalleRequestDTO detalleDTO) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNoEncontradaException(ventaId));

        if (venta.getEstado() != Venta.EstadoVenta.PENDIENTE) {
            throw new VentaNoEncontradaException("Solo se pueden agregar productos a ventas pendientes");
        }

        Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", detalleDTO.getProductoId()));

        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(detalleDTO.getCantidad());
        detalle.setPrecioUnitario(detalleDTO.getPrecioUnitario());
        detalle.setDescuentoPorcentaje(detalleDTO.getDescuentoPorcentaje());
        detalle.calcularSubtotal();

        venta.getDetalles().add(detalle);
        calcularTotal(venta);

        venta = ventaRepository.save(venta);
        return ventaMapper.toResponseDTO(venta);
    }

    public void eliminarDetalle(Long ventaId, Long detalleId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNoEncontradaException(ventaId));

        if (venta.getEstado() != Venta.EstadoVenta.PENDIENTE) {
            throw new VentaNoEncontradaException("Solo se pueden eliminar productos de ventas pendientes");
        }

        DetalleVenta detalle = detalleVentaRepository.findById(detalleId)
                .orElseThrow(() -> new ResourceNotFoundException("DetalleVenta", detalleId));

        venta.removerDetalle(detalle);
        detalleVentaRepository.delete(detalle);

        calcularTotal(venta);
        ventaRepository.save(venta);
    }

    public VentaResponseDTO finalizarVenta(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNoEncontradaException(ventaId));

        if (venta.getEstado() != Venta.EstadoVenta.PENDIENTE) {
            throw new VentaNoEncontradaException("La venta no esta pendiente");
        }

        if (venta.getDetalles().isEmpty()) {
            throw new VentaNoEncontradaException("La venta debe tener al menos un detalle");
        }

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getExistencia() < detalle.getCantidad().intValue()) {
                throw new StockInsuficienteException(producto.getNombre(), producto.getExistencia(),
                        detalle.getCantidad().intValue());
            }
            producto.setExistencia(producto.getExistencia() - detalle.getCantidad().intValue());
            productoRepository.save(producto);
        }

        venta.setEstado(Venta.EstadoVenta.COMPLETADA);
        venta = ventaRepository.save(venta);
        return ventaMapper.toResponseDTO(venta);
    }

    private PageResponseDTO<VentaResponseDTO> buildPageResponse(Page<Venta> page) {
        return PageResponseDTO.<VentaResponseDTO>builder()
                .content(page.getContent().stream().map(ventaMapper::toResponseDTO).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .empty(page.isEmpty())
                .build();
    }
}
