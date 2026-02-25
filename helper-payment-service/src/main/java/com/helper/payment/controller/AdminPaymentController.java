package com.helper.payment.controller;

import com.helper.payment.dto.request.ConfigUpdateRequest;
import com.helper.payment.dto.response.ApiResponse;
import com.helper.payment.dto.response.PaymentResponse;
import com.helper.payment.dto.response.PaymentStatsResponse;
import com.helper.payment.entity.PlatformConfig;
import com.helper.payment.enums.PaymentStatus;
import com.helper.payment.repository.PaymentRepository;
import com.helper.payment.security.AuthenticatedUser;
import com.helper.payment.service.PaymentService;
import com.helper.payment.service.PlatformConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Payments & Config", description = "Revenue stats, transaction management, platform config (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;
    private final PlatformConfigService configService;
    private final PaymentRepository paymentRepo;

    // ===== STATS =====

    @GetMapping("/payments/stats")
    @Operation(summary = "Revenue & commission statistics",
            description = "Total revenue, commission earned, taxes, tips, broken down by method and status")
    public ResponseEntity<ApiResponse<PaymentStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Payment statistics",
                paymentService.getStats()));
    }

    // ===== TRANSACTIONS =====

    @GetMapping("/payments/transactions")
    @Operation(summary = "All transactions", description = "Paginated list with optional status filter")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getTransactions(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> page;
        if (status != null) {
            page = paymentRepo.findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(p -> PaymentResponse.builder()
                            .paymentId(p.getPaymentId()).taskId(p.getTaskId())
                            .payerId(p.getPayerId()).payeeId(p.getPayeeId())
                            .amount(p.getAmount()).commission(p.getCommission())
                            .commissionRate(p.getCommissionRate()).tax(p.getTax())
                            .taxRate(p.getTaxRate()).tip(p.getTip())
                            .workerPayout(p.getWorkerPayout())
                            .method(p.getMethod().name()).status(p.getStatus().name())
                            .invoiceNumber(p.getInvoiceNumber()).invoiceUrl(p.getInvoiceUrl())
                            .processedAt(p.getProcessedAt()).createdAt(p.getCreatedAt())
                            .build());
        } else {
            page = paymentRepo.findAll(pageable).map(p -> PaymentResponse.builder()
                    .paymentId(p.getPaymentId()).taskId(p.getTaskId())
                    .payerId(p.getPayerId()).payeeId(p.getPayeeId())
                    .amount(p.getAmount()).commission(p.getCommission())
                    .commissionRate(p.getCommissionRate()).tax(p.getTax())
                    .taxRate(p.getTaxRate()).tip(p.getTip())
                    .workerPayout(p.getWorkerPayout())
                    .method(p.getMethod().name()).status(p.getStatus().name())
                    .invoiceNumber(p.getInvoiceNumber()).invoiceUrl(p.getInvoiceUrl())
                    .processedAt(p.getProcessedAt()).createdAt(p.getCreatedAt())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.success("Transactions", page));
    }

    // ===== REFUND =====

    @PostMapping("/payments/{paymentId}/refund")
    @Operation(summary = "Refund a payment", description = "Admin initiates refund. Reverses ledger entries.")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam(defaultValue = "Admin initiated refund") String reason,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success("Payment refunded",
                paymentService.refundPayment(paymentId, reason, admin)));
    }

    // ===== PLATFORM CONFIG =====

    @GetMapping("/config")
    @Operation(summary = "Get all platform config values",
            description = "Returns COMMISSION_RATE, GST_RATE, CANCELLATION_FEE_RATE, etc.")
    public ResponseEntity<ApiResponse<List<PlatformConfig>>> getAllConfig() {
        return ResponseEntity.ok(ApiResponse.success("Platform config",
                configService.getAllConfigs()));
    }

    @PutMapping("/config/{key}")
    @Operation(summary = "Update a config value",
            description = "e.g. PUT /admin/config/COMMISSION_RATE with body {\"configValue\": \"0.03\"} changes commission to 3%")
    public ResponseEntity<ApiResponse<PlatformConfig>> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success("Config updated: " + key,
                configService.updateConfig(key, request.getConfigValue(), admin.getUserId())));
    }
}
