package com.pventabase.clientes.entity;

import com.pventabase.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Getter
@Setter
public class Cliente extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String apellido;

    @NotBlank
    @Size(max = 255)
    @Column(name = "domicilio", nullable = false, length = 255)
    private String direccion;

    @Size(max = 20)
    @Column(name = "celular", length = 20)
    private String telefono;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String email;

    @Column(precision = 10, scale = 2)
    private BigDecimal credito;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Size(max = 20)
    @Column(length = 20)
    private String documento;
}
