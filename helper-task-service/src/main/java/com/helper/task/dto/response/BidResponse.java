package com.helper.task.dto.response;

import com.helper.task.enums.BidStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponse {

    private UUID bidId;
    private UUID taskId;
    private UUID workerId;
    private BigDecimal proposedPrice;
    private String message;
    private BidStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
