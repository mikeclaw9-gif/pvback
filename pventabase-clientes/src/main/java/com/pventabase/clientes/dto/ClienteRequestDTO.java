package com.pventabase.clientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ClienteRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @NotBlank
    @Size(max = 255)
    private String direccion;

    @Size(max = 20)
    private String telefono;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    private BigDecimal credito;

    private Boolean activo;

    @Size(max = 20)
    private String documento;
}
