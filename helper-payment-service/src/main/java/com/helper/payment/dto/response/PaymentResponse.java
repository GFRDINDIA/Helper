package com.helper.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID taskId;
    private UUID payerId;
    private UUID payeeId;
    private BigDecimal amount;
    private BigDecimal commission;
    private BigDecimal commissionRate;
    private BigDecimal tax;
    private BigDecimal taxRate;
    private BigDecimal tip;
    private BigDecimal workerPayout;
    private String method;
    private String status;
    private String invoiceNumber;
    private String invoiceUrl;
    private String paymentReference;
    private String notes;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
