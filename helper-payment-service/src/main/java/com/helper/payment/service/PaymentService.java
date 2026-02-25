package com.helper.payment.service;

import com.helper.payment.dto.request.AddTipRequest;
import com.helper.payment.dto.request.InitiatePaymentRequest;
import com.helper.payment.dto.response.LedgerResponse;
import com.helper.payment.dto.response.PaymentResponse;
import com.helper.payment.dto.response.PaymentStatsResponse;
import com.helper.payment.entity.Payment;
import com.helper.payment.entity.WorkerLedgerEntry;
import com.helper.payment.enums.LedgerEntryType;
import com.helper.payment.enums.PaymentMethod;
import com.helper.payment.enums.PaymentStatus;
import com.helper.payment.exception.PaymentExceptions;
import com.helper.payment.repository.PaymentRepository;
import com.helper.payment.repository.WorkerLedgerRepository;
import com.helper.payment.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final WorkerLedgerRepository ledgerRepo;
    private final PlatformConfigService configService;
    private final InvoiceService invoiceService;

    @Value("${app.payment.invoice-prefix:HLP-INV}")
    private String invoicePrefix;

    private final AtomicLong invoiceCounter = new AtomicLong(0);

    // ===== INITIATE PAYMENT =====
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID customerId, UUID workerId,
                                            BigDecimal finalPrice, AuthenticatedUser user) {
        // Validate no duplicate payment for this task
        if (paymentRepo.existsByTaskId(request.getTaskId())) {
            throw new PaymentExceptions.DuplicatePaymentException(
                    "Payment already exists for task: " + request.getTaskId());
        }

        // Validate caller is the customer
        if (!user.getUserId().equals(customerId)) {
            throw new PaymentExceptions.UnauthorizedPaymentAccessException("Only the task customer can initiate payment");
        }

        // Read configurable rates from platform_config
        BigDecimal commissionRate = configService.getCommissionRate();
        BigDecimal gstRate = configService.getGstRate();

        // Calculate payment breakdown
        PaymentCalculator.PaymentBreakdown breakdown = PaymentCalculator.calculate(
                finalPrice, request.getTipAmount(), commissionRate, gstRate);

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Create payment record
        Payment payment = Payment.builder()
                .taskId(request.getTaskId())
                .payerId(customerId)
                .payeeId(workerId)
                .amount(breakdown.getAmount())
                .commission(breakdown.getCommission())
                .commissionRate(breakdown.getCommissionRate())
                .tax(breakdown.getTax())
                .taxRate(breakdown.getTaxRate())
                .tip(breakdown.getTip())
                .workerPayout(breakdown.getWorkerPayout())
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .invoiceNumber(invoiceNumber)
                .build();

        // For CASH: worker already has the money, set to PENDING until worker confirms
        // For digital: will remain PENDING until Razorpay callback (Phase 2)
        payment = paymentRepo.save(payment);

        // Create ledger entry for commission owed
        addLedgerEntry(workerId, payment.getPaymentId(), LedgerEntryType.COMMISSION_DUE,
                breakdown.getTotalDeduction(),
                "Commission + GST for task " + request.getTaskId());

        // Generate invoice
        try {
            String invoiceUrl = invoiceService.generateInvoice(payment);
            payment.setInvoiceUrl(invoiceUrl);
            payment = paymentRepo.save(payment);
        } catch (Exception e) {
            log.warn("Invoice generation failed for payment {}: {}", payment.getPaymentId(), e.getMessage());
        }

        log.info("Payment initiated: {} for task: {} method: {} amount: {} commission: {} tax: {} payout: {}",
                payment.getPaymentId(), request.getTaskId(), request.getMethod(),
                breakdown.getAmount(), breakdown.getCommission(), breakdown.getTax(), breakdown.getWorkerPayout());

        return mapToResponse(payment);
    }

    // ===== CONFIRM CASH PAYMENT =====
    @Transactional
    public PaymentResponse confirmCashPayment(UUID paymentId, AuthenticatedUser user) {
        Payment payment = getPaymentOrThrow(paymentId);

        if (payment.getMethod() != PaymentMethod.CASH) {
            throw new PaymentExceptions.InvalidPaymentStateException("Only cash payments need manual confirmation");
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentExceptions.InvalidPaymentStateException("Payment is not pending. Status: " + payment.getStatus());
        }
        if (!payment.getPayeeId().equals(user.getUserId())) {
            throw new PaymentExceptions.UnauthorizedPaymentAccessException("Only the assigned worker can confirm cash receipt");
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProcessedAt(LocalDateTime.now());
        payment = paymentRepo.save(payment);

        log.info("Cash payment confirmed: {} by worker: {}", paymentId, user.getUserId());
        return mapToResponse(payment);
    }

    // ===== ADD TIP =====
    @Transactional
    public PaymentResponse addTip(UUID paymentId, AddTipRequest request, AuthenticatedUser user) {
        Payment payment = getPaymentOrThrow(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED && payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentExceptions.InvalidPaymentStateException("Cannot add tip to " + payment.getStatus() + " payment");
        }
        if (!payment.getPayerId().equals(user.getUserId())) {
            throw new PaymentExceptions.UnauthorizedPaymentAccessException("Only the customer can add a tip");
        }

        BigDecimal newTip = payment.getTip().add(request.getTipAmount());
        payment.setTip(newTip);
        // Tip goes 100% to worker — recalculate payout
        BigDecimal basePayout = payment.getAmount().subtract(payment.getCommission()).subtract(payment.getTax());
        payment.setWorkerPayout(basePayout.add(newTip));
        payment = paymentRepo.save(payment);

        log.info("Tip added: {} to payment: {} total tip now: {}", request.getTipAmount(), paymentId, newTip);
        return mapToResponse(payment);
    }

    // ===== GET PAYMENT =====
    public PaymentResponse getPayment(UUID paymentId, AuthenticatedUser user) {
        Payment payment = getPaymentOrThrow(paymentId);
        validateAccess(payment, user);
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByTaskId(UUID taskId, AuthenticatedUser user) {
        Payment payment = paymentRepo.findByTaskId(taskId)
                .orElseThrow(() -> new PaymentExceptions.PaymentNotFoundException("No payment found for task: " + taskId));
        validateAccess(payment, user);
        return mapToResponse(payment);
    }

    // ===== TRANSACTION HISTORY =====
    public Page<PaymentResponse> getMyTransactions(AuthenticatedUser user, Pageable pageable) {
        return paymentRepo.findByPayerIdOrPayeeIdOrderByCreatedAtDesc(
                user.getUserId(), user.getUserId(), pageable).map(this::mapToResponse);
    }

    // ===== REFUND (ADMIN) =====
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, String reason, AuthenticatedUser admin) {
        Payment payment = getPaymentOrThrow(paymentId);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentExceptions.InvalidPaymentStateException("Can only refund completed payments");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setNotes("Refund by admin: " + admin.getUserId() + ". Reason: " + reason);
        payment = paymentRepo.save(payment);

        // Reverse ledger entry
        addLedgerEntry(payment.getPayeeId(), payment.getPaymentId(), LedgerEntryType.COMMISSION_PAID,
                payment.getCommission().add(payment.getTax()).negate(),
                "Refund reversal for task " + payment.getTaskId());

        log.info("Payment refunded: {} by admin: {} reason: {}", paymentId, admin.getUserId(), reason);
        return mapToResponse(payment);
    }

    // ===== WORKER LEDGER =====
    public LedgerResponse getWorkerLedger(UUID workerId, Pageable pageable) {
        Page<WorkerLedgerEntry> entries = ledgerRepo.findByWorkerIdOrderByCreatedAtDesc(workerId, pageable);
        BigDecimal balance = ledgerRepo.findCurrentBalance(workerId).orElse(BigDecimal.ZERO);

        List<LedgerResponse.LedgerEntry> entryDtos = entries.getContent().stream()
                .map(e -> LedgerResponse.LedgerEntry.builder()
                        .ledgerId(e.getLedgerId())
                        .paymentId(e.getPaymentId())
                        .type(e.getType().name())
                        .amount(e.getAmount())
                        .balanceAfter(e.getBalanceAfter())
                        .description(e.getDescription())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return LedgerResponse.builder().currentBalance(balance).entries(entryDtos).build();
    }

    public BigDecimal getWorkerBalance(UUID workerId) {
        return ledgerRepo.findCurrentBalance(workerId).orElse(BigDecimal.ZERO);
    }

    // ===== ADMIN STATS =====
    public PaymentStatsResponse getStats() {
        Map<String, Long> byStatus = new HashMap<>();
        for (PaymentStatus s : PaymentStatus.values()) {
            byStatus.put(s.name(), paymentRepo.countByStatus(s));
        }

        Map<String, Long> byMethod = new HashMap<>();
        for (PaymentMethod m : PaymentMethod.values()) {
            byMethod.put(m.name(), paymentRepo.countByMethodAndCompleted(m));
        }

        return PaymentStatsResponse.builder()
                .totalTransactions(paymentRepo.count())
                .totalRevenue(paymentRepo.sumTotalRevenue())
                .totalCommission(paymentRepo.sumTotalCommission())
                .totalTax(paymentRepo.sumTotalTax())
                .totalTips(paymentRepo.sumTotalTips())
                .byStatus(byStatus)
                .byMethod(byMethod)
                .build();
    }

    // ===== PRIVATE HELPERS =====

    private Payment getPaymentOrThrow(UUID paymentId) {
        return paymentRepo.findById(paymentId)
                .orElseThrow(() -> new PaymentExceptions.PaymentNotFoundException("Payment not found: " + paymentId));
    }

    private void validateAccess(Payment payment, AuthenticatedUser user) {
        if (user.isAdmin()) return;
        if (!payment.getPayerId().equals(user.getUserId()) && !payment.getPayeeId().equals(user.getUserId())) {
            throw new PaymentExceptions.UnauthorizedPaymentAccessException("You don't have access to this payment");
        }
    }

    private void addLedgerEntry(UUID workerId, UUID paymentId, LedgerEntryType type,
                                 BigDecimal amount, String description) {
        BigDecimal currentBalance = ledgerRepo.findCurrentBalance(workerId).orElse(BigDecimal.ZERO);
        BigDecimal newBalance = currentBalance.add(amount);

        WorkerLedgerEntry entry = WorkerLedgerEntry.builder()
                .workerId(workerId)
                .paymentId(paymentId)
                .type(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .description(description)
                .build();
        ledgerRepo.save(entry);
    }

    private String generateInvoiceNumber() {
        long seq = invoiceCounter.incrementAndGet();
        return String.format("%s-%d-%06d", invoicePrefix, Year.now().getValue(), seq);
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId()).taskId(p.getTaskId())
                .payerId(p.getPayerId()).payeeId(p.getPayeeId())
                .amount(p.getAmount()).commission(p.getCommission())
                .commissionRate(p.getCommissionRate()).tax(p.getTax())
                .taxRate(p.getTaxRate()).tip(p.getTip())
                .workerPayout(p.getWorkerPayout())
                .method(p.getMethod().name()).status(p.getStatus().name())
                .invoiceNumber(p.getInvoiceNumber()).invoiceUrl(p.getInvoiceUrl())
                .paymentReference(p.getPaymentReference()).notes(p.getNotes())
                .processedAt(p.getProcessedAt()).createdAt(p.getCreatedAt())
                .build();
    }
}
