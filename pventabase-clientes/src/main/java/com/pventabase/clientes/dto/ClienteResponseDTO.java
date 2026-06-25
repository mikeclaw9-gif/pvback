package com.pventabase.clientes.dto;

import com.pventabase.common.dto.BaseResponseDTO;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ClienteResponseDTO extends BaseResponseDTO {

    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String email;
    private BigDecimal credito;
    private Boolean eliminado;
    private String documento;
}
