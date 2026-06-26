package com.pventabase.ventas.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
    "com.pventabase.clientes.entity",
    "com.pventabase.inventario.entity",
    "com.pventabase.ventas.domain",
    "com.pventabase.usuarios.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.pventabase.clientes.repository",
    "com.pventabase.inventario.repository",
    "com.pventabase.ventas.repository"
})
public class TestJpaConfig {
}