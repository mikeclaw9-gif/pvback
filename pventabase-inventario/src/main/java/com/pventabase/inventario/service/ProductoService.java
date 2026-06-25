package com.pventabase.inventario.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.DuplicateResourceException;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.inventario.dto.ProductoRequestDTO;
import com.pventabase.inventario.dto.ProductoResponseDTO;
import com.pventabase.inventario.entity.Producto;
import com.pventabase.inventario.mapper.ProductoMapper;
import com.pventabase.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<ProductoResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productoPage = productoRepository.findAll(pageable);
        return buildPageResponse(productoPage);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        return productoMapper.toResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findByCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "codigo", codigo));
        return productoMapper.toResponseDTO(producto);
    }

    public ProductoResponseDTO create(ProductoRequestDTO requestDTO) {
        if (productoRepository.existsByCodigo(requestDTO.getCodigo())) {
            throw new DuplicateResourceException("Producto", "codigo", requestDTO.getCodigo());
        }
        Producto producto = productoMapper.toEntity(requestDTO);
        producto = productoRepository.save(producto);
        return productoMapper.toResponseDTO(producto);
    }

    public ProductoResponseDTO update(Long id, ProductoRequestDTO requestDTO) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        if (!producto.getCodigo().equals(requestDTO.getCodigo())
                && productoRepository.existsByCodigo(requestDTO.getCodigo())) {
            throw new DuplicateResourceException("Producto", "codigo", requestDTO.getCodigo());
        }
        productoMapper.updateEntity(requestDTO, producto);
        producto = productoRepository.save(producto);
        return productoMapper.toResponseDTO(producto);
    }

    public void delete(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto", id);
        }
        productoRepository.deleteById(id);
    }

    public ProductoResponseDTO toggleActivo(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        producto.setActivo(!producto.getActivo());
        producto = productoRepository.save(producto);
        return productoMapper.toResponseDTO(producto);
    }

    private PageResponseDTO<ProductoResponseDTO> buildPageResponse(Page<Producto> page) {
        return PageResponseDTO.<ProductoResponseDTO>builder()
                .content(page.getContent().stream().map(productoMapper::toResponseDTO).toList())
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
