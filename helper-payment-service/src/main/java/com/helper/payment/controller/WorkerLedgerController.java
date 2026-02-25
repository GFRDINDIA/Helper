package com.helper.payment.controller;

import com.helper.payment.dto.response.ApiResponse;
import com.helper.payment.dto.response.LedgerResponse;
import com.helper.payment.security.AuthenticatedUser;
import com.helper.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/workers/ledger")
@RequiredArgsConstructor
@Tag(name = "Worker Ledger", description = "Commission owed/paid history and current balance")
@SecurityRequirement(name = "bearerAuth")
public class WorkerLedgerController {

    private final PaymentService paymentService;

    @GetMapping
    @Operation(summary = "Worker's commission ledger",
            description = "Shows all ledger entries (COMMISSION_DUE, COMMISSION_PAID, etc.) with running balance. " +
                    "Positive balance = amount owed to platform from cash payment commissions.")
    public ResponseEntity<ApiResponse<LedgerResponse>> getLedger(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Your ledger",
                paymentService.getWorkerLedger(user.getUserId(), pageable)));
    }

    @GetMapping("/balance")
    @Operation(summary = "Current outstanding balance",
            description = "Amount the worker owes the platform from cash payment commissions. " +
                    "Will be auto-deducted when Razorpay digital payments are integrated in Phase 2.")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Current balance",
                paymentService.getWorkerBalance(user.getUserId())));
    }
}
