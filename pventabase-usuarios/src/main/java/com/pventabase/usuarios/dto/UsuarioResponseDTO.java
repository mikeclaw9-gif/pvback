package com.pventabase.usuarios.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import com.pventabase.usuarios.entity.RolUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioResponseDTO extends BaseResponseDTO {

    private String nombre;
    private String apellido;
    private String email;
    private RolUsuario rol;
    private String telefono;
}
