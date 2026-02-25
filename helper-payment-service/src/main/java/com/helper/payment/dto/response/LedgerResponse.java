package com.helper.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LedgerResponse {
    private BigDecimal currentBalance;
    private List<LedgerEntry> entries;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LedgerEntry {
        private UUID ledgerId;
        private UUID paymentId;
        private String type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String description;
        private LocalDateTime createdAt;
    }
}
