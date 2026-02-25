package com.helper.notification.config;

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
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Helper Notification Service API")
                .description("Push (Firebase), SMS, Email, In-App notifications for Helper marketplace lifecycle events.")
                .version("1.0.0").contact(new Contact().name("Grace and Faith R&D").email("dev@graceandfaith.in")))
            .servers(List.of(new Server().url("http://localhost:8086").description("Local Dev")));
    }
}
