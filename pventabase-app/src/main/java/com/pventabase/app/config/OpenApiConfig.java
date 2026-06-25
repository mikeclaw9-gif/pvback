package com.pventabase.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("pventabase API")
                        .description("Point of Sale API")
                        .version("1.0-SNAPSHOT")
                        .contact(new Contact()
                                .name("Desarrollador")
                                .email("dev@pventabase.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public GroupedOpenApi usuariosApi() {
        return GroupedOpenApi.builder()
                .group("usuarios")
                .pathsToMatch("/usuarios/**")
                .build();
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
                .group("login")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi inventarioApi() {
        return GroupedOpenApi.builder()
                .group("inventario")
                .pathsToMatch("/productos/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clientesApi() {
        return GroupedOpenApi.builder()
                .group("clientes")
                .pathsToMatch("/clientes/**")
                .build();
    }
}
