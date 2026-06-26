package com.pventabase.ventas.api.mapper;

import com.pventabase.ventas.api.dto.DetalleVentaResponseDTO;
import com.pventabase.ventas.api.dto.VentaRequestDTO;
import com.pventabase.ventas.api.dto.VentaResponseDTO;
import com.pventabase.ventas.domain.DetalleVenta;
import com.pventabase.ventas.domain.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNombre", expression = "java(venta.getCliente() != null ? venta.getCliente().getNombre() + \" \" + venta.getCliente().getApellido() : null)")
    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "detalles", source = "detalles")
    VentaResponseDTO toResponseDTO(Venta venta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Venta toEntity(VentaRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void updateEntity(VentaRequestDTO requestDTO, @MappingTarget Venta venta);
}
