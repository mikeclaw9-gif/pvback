package com.pventabase.cortecaja.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.cortecaja.dto.AbrirCorteRequestDTO;
import com.pventabase.cortecaja.dto.CerrarCorteRequestDTO;
import com.pventabase.cortecaja.dto.CorteCajaResponseDTO;
import com.pventabase.cortecaja.entity.CorteCaja;
import com.pventabase.cortecaja.enums.EstadoCorte;
import com.pventabase.cortecaja.mapper.CorteCajaMapper;
import com.pventabase.cortecaja.repository.CorteCajaRepository;
import com.pventabase.gastos.repository.GastoRepository;
import com.pventabase.usuarios.entity.Usuario;
import com.pventabase.usuarios.repository.UsuarioRepository;
import com.pventabase.ventas.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CorteCajaService {

    private final CorteCajaRepository corteCajaRepository;
    private final CorteCajaMapper corteCajaMapper;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final GastoRepository gastoRepository;

    @Transactional(readOnly = true)
    public PageResponseDTO<CorteCajaResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CorteCaja> cortePage = corteCajaRepository.findAll(pageable);
        return buildPageResponse(cortePage);
    }

    @Transactional(readOnly = true)
    public CorteCajaResponseDTO findById(Long id) {
        CorteCaja corte = corteCajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CorteCaja", id));
        return corteCajaMapper.toResponseDTO(corte);
    }

    @Transactional(readOnly = true)
    public CorteCajaResponseDTO findCorteAbierto() {
        CorteCaja corte = corteCajaRepository.findTopByEstadoOrderByFechaAperturaDesc(EstadoCorte.ABIERTO)
                .orElseThrow(() -> new ResourceNotFoundException("CorteCaja", "No hay ningun corte abierto"));
        return corteCajaMapper.toResponseDTO(corte);
    }

    public CorteCajaResponseDTO abrirCorte(AbrirCorteRequestDTO requestDTO, String usuarioEmail) {
        if (corteCajaRepository.existsByEstado(EstadoCorte.ABIERTO)) {
            throw new IllegalStateException("Ya existe un corte de caja abierto. Debe cerrarlo antes de abrir uno nuevo.");
        }

        Usuario usuario = usuarioRepository.findByEmail(usuarioEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", usuarioEmail));

        CorteCaja corte = new CorteCaja();
        corte.setFechaApertura(LocalDateTime.now());
        corte.setMontoInicial(requestDTO.getMontoInicial());
        corte.setObservacion(requestDTO.getObservacion());
        corte.setEstado(EstadoCorte.ABIERTO);
        corte.setUsuario(usuario);

        corte = corteCajaRepository.save(corte);
        return corteCajaMapper.toResponseDTO(corte);
    }

    public CorteCajaResponseDTO cerrarCorte(Long id, CerrarCorteRequestDTO requestDTO) {
        CorteCaja corte = corteCajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CorteCaja", id));

        if (corte.getEstado() != EstadoCorte.ABIERTO) {
            throw new IllegalStateException("El corte de caja ya esta cerrado");
        }

        LocalDateTime inicio = corte.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();

        BigDecimal totalVentas = ventaRepository.sumTotalCompletadasEntreFechas(inicio, fin);
        BigDecimal totalGastos = gastoRepository.sumMontoEntreFechas(inicio, fin);

        List<Object[]> ventasPorMetodo = ventaRepository.sumTotalGroupedByMetodoPagoEntreFechas(inicio, fin);
        Map<String, BigDecimal> metodoMap = ventasPorMetodo.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        BigDecimal totalEfectivo = metodoMap.getOrDefault("EFECTIVO", BigDecimal.ZERO);
        BigDecimal totalTarjeta = metodoMap.getOrDefault("TARJETA_DEBITO", BigDecimal.ZERO)
                .add(metodoMap.getOrDefault("TARJETA_CREDITO", BigDecimal.ZERO));
        BigDecimal totalTransferencia = metodoMap.getOrDefault("TRANSFERENCIA", BigDecimal.ZERO);

        BigDecimal montoEsperado = corte.getMontoInicial()
                .add(totalVentas)
                .subtract(totalGastos);
        BigDecimal diferencia = requestDTO.getMontoFinal().subtract(montoEsperado);

        corte.setFechaCierre(fin);
        corte.setMontoFinal(requestDTO.getMontoFinal());
        corte.setTotalVentas(totalVentas);
        corte.setTotalGastos(totalGastos);
        corte.setTotalEfectivo(totalEfectivo);
        corte.setTotalTarjeta(totalTarjeta);
        corte.setTotalTransferencia(totalTransferencia);
        corte.setDiferencia(diferencia);
        corte.setObservacion(requestDTO.getObservacion());
        corte.setEstado(EstadoCorte.CERRADO);

        corte = corteCajaRepository.save(corte);
        return corteCajaMapper.toResponseDTO(corte);
    }

    private PageResponseDTO<CorteCajaResponseDTO> buildPageResponse(Page<CorteCaja> page) {
        return PageResponseDTO.<CorteCajaResponseDTO>builder()
                .content(page.getContent().stream().map(corteCajaMapper::toResponseDTO).toList())
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
