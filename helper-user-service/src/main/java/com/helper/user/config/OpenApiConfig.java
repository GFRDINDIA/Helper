package com.helper.user.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Helper User Profile & KYC Service API")
                .description("Worker profiles, customer profiles, skills management, KYC verification, portfolio, and availability.")
                .version("1.0.0").contact(new Contact().name("Grace and Faith R&D").email("dev@graceandfaith.in")))
            .servers(List.of(new Server().url("http://localhost:8083").description("Local Dev")));
    }
}
