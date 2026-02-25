package com.helper.auth.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter your JWT access token"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI helperAuthOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helper Auth Service API")
                        .description("Authentication and authorization service for the Helper marketplace platform. " +
                                "Handles user registration, email OTP verification, JWT-based login, " +
                                "password management, and admin user management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Grace and Faith R&D")
                                .email("dev@graceandfaith.in"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://helper.app/terms")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development"),
                        new Server().url("https://api.helper.app").description("Production")
                ));
    }
}
