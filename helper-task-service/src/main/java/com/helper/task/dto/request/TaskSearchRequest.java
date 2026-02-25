package com.helper.task.dto.request;

import com.helper.task.enums.PricingModel;
import com.helper.task.enums.TaskDomain;
import com.helper.task.enums.TaskStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchRequest {

    private Double latitude;
    private Double longitude;

    @Builder.Default
    private Double radiusKm = 10.0;

    private TaskDomain domain;
    private TaskStatus status;
    private PricingModel pricingModel;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}
