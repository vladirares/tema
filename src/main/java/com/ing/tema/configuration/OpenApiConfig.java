// src/main/java/com/example/store/config/OpenApiConfig.java
package com.ing.tema.configuration;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Store Management API",
                version = "1.0",
                description = "API for managing products in a store"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local")
        },
        security = {
                @SecurityRequirement(name = "bearer-jwt")
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearer-jwt",
                                        new io.swagger.v3.oas.models.security.SecurityScheme()
                                                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .addSecurityItem(
                        new io.swagger.v3.oas.models.security.SecurityRequirement()
                                .addList("bearer-jwt")
                );
    }
}
