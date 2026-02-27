package com.helper.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.gateway")
@Data
public class AppGatewayProperties {
    private List<String> openPaths;
}
