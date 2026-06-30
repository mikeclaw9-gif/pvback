package com.pventabase.gastos.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.gastos.dto.GastoRequestDTO;
import com.pventabase.gastos.dto.GastoResponseDTO;
import com.pventabase.gastos.entity.Gasto;
import com.pventabase.gastos.mapper.GastoMapper;
import com.pventabase.gastos.repository.GastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GastoService {

    private final GastoRepository gastoRepository;
    private final GastoMapper gastoMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<GastoResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Gasto> gastoPage = gastoRepository.findAll(pageable);
        return buildPageResponse(gastoPage);
    }

    @Transactional(readOnly = true)
    public GastoResponseDTO findById(Long id) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto", id));
        return gastoMapper.toResponseDTO(gasto);
    }

    @Transactional(readOnly = true)
    public List<GastoResponseDTO> findByCategoria(String categoria) {
        return gastoRepository.findByCategoria(categoria).stream()
                .map(gastoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GastoResponseDTO> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return gastoRepository.findByFechaGastoBetween(inicio, fin).stream()
                .map(gastoMapper::toResponseDTO)
                .toList();
    }

    public GastoResponseDTO create(GastoRequestDTO requestDTO) {
        Gasto gasto = gastoMapper.toEntity(requestDTO);
        gasto = gastoRepository.save(gasto);
        return gastoMapper.toResponseDTO(gasto);
    }

    public GastoResponseDTO update(Long id, GastoRequestDTO requestDTO) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto", id));
        gastoMapper.updateEntity(requestDTO, gasto);
        gasto = gastoRepository.save(gasto);
        return gastoMapper.toResponseDTO(gasto);
    }

    public void delete(Long id) {
        if (!gastoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gasto", id);
        }
        gastoRepository.deleteById(id);
    }

    public GastoResponseDTO toggleActivo(Long id) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto", id));
        gasto.setActivo(!gasto.getActivo());
        gasto = gastoRepository.save(gasto);
        return gastoMapper.toResponseDTO(gasto);
    }

    private PageResponseDTO<GastoResponseDTO> buildPageResponse(Page<Gasto> page) {
        return PageResponseDTO.<GastoResponseDTO>builder()
                .content(page.getContent().stream().map(gastoMapper::toResponseDTO).toList())
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
