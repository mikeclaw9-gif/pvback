package com.pventabase.ventas.api.mapper;

import com.pventabase.ventas.api.dto.DetalleVentaRequestDTO;
import com.pventabase.ventas.api.dto.DetalleVentaResponseDTO;
import com.pventabase.ventas.domain.DetalleVenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DetalleVentaMapper {

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoCodigo", source = "producto.codigo")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "productoPesado", source = "producto.pesado")
    DetalleVentaResponseDTO toResponseDTO(DetalleVenta detalleVenta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    DetalleVenta toEntity(DetalleVentaRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "venta", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    void updateEntity(DetalleVentaRequestDTO requestDTO, @MappingTarget DetalleVenta detalleVenta);
}