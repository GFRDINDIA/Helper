package com.helper.payment.controller;

import com.helper.payment.dto.request.AddTipRequest;
import com.helper.payment.dto.request.InitiatePaymentRequest;
import com.helper.payment.dto.response.ApiResponse;
import com.helper.payment.dto.response.PaymentResponse;
import com.helper.payment.security.AuthenticatedUser;
import com.helper.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initiation, confirmation, tips, and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for a completed task",
            description = "Customer initiates payment after task completion. " +
                    "System calculates 2% commission + 18% GST on commission. " +
                    "For cash: payment is created as PENDING until worker confirms receipt.")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestParam UUID customerId,
            @RequestParam UUID workerId,
            @RequestParam BigDecimal finalPrice,
            @AuthenticationPrincipal AuthenticatedUser user) {
        // NOTE: In production, customerId/workerId/finalPrice come from Task Service
        // via inter-service call. For MVP, passed as params for independent testing.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated",
                        paymentService.initiatePayment(request, customerId, workerId, finalPrice, user)));
    }

    @PutMapping("/{paymentId}/confirm")
    @Operation(summary = "Worker confirms cash received",
            description = "For cash payments, the worker confirms they received payment from the customer. " +
                    "This moves payment status from PENDING to COMPLETED.")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmCashPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Cash payment confirmed",
                paymentService.confirmCashPayment(paymentId, user)));
    }

    @PostMapping("/{paymentId}/tip")
    @Operation(summary = "Add tip to a payment",
            description = "Customer adds a tip after payment. Tip goes 100% to worker with ZERO commission.")
    public ResponseEntity<ApiResponse<PaymentResponse>> addTip(
            @PathVariable UUID paymentId,
            @Valid @RequestBody AddTipRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Tip added",
                paymentService.addTip(paymentId, request, user)));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details", description = "Only payer, payee, or admin can view")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Payment details",
                paymentService.getPayment(paymentId, user)));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get payment for a specific task")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Payment for task",
                paymentService.getPaymentByTaskId(taskId, user)));
    }

    @GetMapping("/my-transactions")
    @Operation(summary = "My transaction history",
            description = "Returns paginated list of all payments where the user is either payer or payee")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyTransactions(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Your transactions",
                paymentService.getMyTransactions(user, pageable)));
    }

    @GetMapping("/invoices/{paymentId}")
    @Operation(summary = "Get invoice URL for a payment")
    public ResponseEntity<ApiResponse<String>> getInvoice(
            @PathVariable UUID paymentId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        PaymentResponse payment = paymentService.getPayment(paymentId, user);
        return ResponseEntity.ok(ApiResponse.success("Invoice", payment.getInvoiceUrl()));
    }

    // Phase 2 placeholder
    @PostMapping("/callback")
    @Operation(summary = "[Phase 2] Razorpay payment webhook callback", description = "Not implemented in MVP")
    public ResponseEntity<ApiResponse<Void>> razorpayCallback() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Digital payments not yet available", "NOT_IMPLEMENTED"));
    }
}
