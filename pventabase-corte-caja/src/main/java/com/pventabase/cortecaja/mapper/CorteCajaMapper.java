package com.pventabase.cortecaja.mapper;

import com.pventabase.cortecaja.dto.CorteCajaResponseDTO;
import com.pventabase.cortecaja.entity.CorteCaja;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CorteCajaMapper {

    @Mapping(target = "usuarioEmail", source = "usuario.email")
    @Mapping(target = "usuarioNombre", expression = "java(corteCaja.getUsuario() != null ? corteCaja.getUsuario().getNombre() + \" \" + corteCaja.getUsuario().getApellido() : null)")
    CorteCajaResponseDTO toResponseDTO(CorteCaja corteCaja);
}
