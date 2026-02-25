package com.helper.user.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioItemResponse {
    private UUID itemId;
    private String imageUrl;
    private String description;
    private String domain;
    private LocalDateTime createdAt;
}
