package com.helper.user.dto.request;

import com.helper.user.enums.TaskDomain;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioRequest {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private String description;
    private TaskDomain domain;
}
