package com.pventabase.clientes.service;

import com.pventabase.common.dto.PageResponseDTO;
import com.pventabase.common.exception.DuplicateResourceException;
import com.pventabase.common.exception.ResourceNotFoundException;
import com.pventabase.clientes.dto.ClienteRequestDTO;
import com.pventabase.clientes.dto.ClienteResponseDTO;
import com.pventabase.clientes.entity.Cliente;
import com.pventabase.clientes.mapper.ClienteMapper;
import com.pventabase.clientes.repository.ClienteRepository;
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
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    public PageResponseDTO<ClienteResponseDTO> findAll(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Cliente> clientePage = clienteRepository.findAll(pageable);
        return buildPageResponse(clientePage);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return clienteMapper.toResponseDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findByEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", email));
        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO create(ClienteRequestDTO requestDTO) {
        if (clienteRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Cliente", "email", requestDTO.getEmail());
        }
        Cliente cliente = clienteMapper.toEntity(requestDTO);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO update(Long id, ClienteRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        if (!cliente.getEmail().equals(requestDTO.getEmail())
                && clienteRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DuplicateResourceException("Cliente", "email", requestDTO.getEmail());
        }
        clienteMapper.updateEntity(requestDTO, cliente);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clienteRepository.deleteById(id);
    }

    public ClienteResponseDTO toggleActivo(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        cliente.setActivo(!cliente.getActivo());
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    public ClienteResponseDTO marcarEliminado(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        cliente.setEliminado(true);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponseDTO(cliente);
    }

    private PageResponseDTO<ClienteResponseDTO> buildPageResponse(Page<Cliente> page) {
        return PageResponseDTO.<ClienteResponseDTO>builder()
                .content(page.getContent().stream().map(clienteMapper::toResponseDTO).toList())
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
