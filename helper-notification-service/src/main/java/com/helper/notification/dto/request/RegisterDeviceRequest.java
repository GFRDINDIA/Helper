package com.helper.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterDeviceRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    @Builder.Default
    private String platform = "ANDROID"; // ANDROID, IOS, WEB

    private String deviceName;
}
