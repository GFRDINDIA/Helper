package com.helper.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentStatsResponse {
    private long totalTransactions;
    private BigDecimal totalRevenue;
    private BigDecimal totalCommission;
    private BigDecimal totalTax;
    private BigDecimal totalTips;
    private Map<String, Long> byMethod;
    private Map<String, Long> byStatus;
}
