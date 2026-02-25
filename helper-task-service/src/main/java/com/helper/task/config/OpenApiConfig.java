package com.helper.task.config;

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
    public OpenAPI taskServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Helper Task Service API")
                        .description("Task management, geo-search, bidding, and lifecycle service for Helper marketplace.")
                        .version("1.0.0")
                        .contact(new Contact().name("Grace and Faith R&D").email("dev@graceandfaith.in")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local Development"),
                        new Server().url("https://api.helper.app").description("Production")));
    }
}
