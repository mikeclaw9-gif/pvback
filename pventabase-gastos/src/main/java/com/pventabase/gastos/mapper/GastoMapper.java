package com.pventabase.gastos.mapper;

import com.pventabase.gastos.dto.GastoRequestDTO;
import com.pventabase.gastos.dto.GastoResponseDTO;
import com.pventabase.gastos.entity.Gasto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GastoMapper {

    GastoResponseDTO toResponseDTO(Gasto gasto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Gasto toEntity(GastoRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntity(GastoRequestDTO requestDTO, @MappingTarget Gasto gasto);
}
