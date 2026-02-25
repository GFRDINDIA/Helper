package com.helper.payment.config;

import com.helper.payment.entity.Payment;
import com.helper.payment.entity.PlatformConfig;
import com.helper.payment.entity.WorkerLedgerEntry;
import com.helper.payment.enums.LedgerEntryType;
import com.helper.payment.enums.PaymentMethod;
import com.helper.payment.enums.PaymentStatus;
import com.helper.payment.repository.PaymentRepository;
import com.helper.payment.repository.PlatformConfigRepository;
import com.helper.payment.repository.WorkerLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PaymentRepository paymentRepo;
    private final PlatformConfigRepository configRepo;
    private final WorkerLedgerRepository ledgerRepo;

    // Match UUIDs from User Service sample data
    private static final UUID CUSTOMER_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WORKER_1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID WORKER_2 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID TASK_1 = UUID.fromString("00000000-0000-0000-0000-000000000100");
    private static final UUID TASK_2 = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID TASK_3 = UUID.fromString("00000000-0000-0000-0000-000000000102");

    @Override
    public void run(String... args) {
        // Seed platform config
        if (configRepo.count() == 0) {
            configRepo.save(PlatformConfig.builder()
                    .configKey("COMMISSION_RATE").configValue("0.02")
                    .description("Platform commission rate (2%)").build());
            configRepo.save(PlatformConfig.builder()
                    .configKey("GST_RATE").configValue("0.18")
                    .description("GST rate on commission (18%)").build());
            configRepo.save(PlatformConfig.builder()
                    .configKey("CANCELLATION_FEE_RATE").configValue("0.10")
                    .description("Cancellation fee rate (10%)").build());
        }

        if (paymentRepo.count() > 0) return;

        // Payment 1: Completed cash payment - Plumbing task ₹500
        // Commission: 500 * 0.02 = 10, GST: 10 * 0.18 = 1.80, Payout: 500 - 10 - 1.80 = 488.20
        Payment p1 = paymentRepo.save(Payment.builder()
                .taskId(TASK_1).payerId(CUSTOMER_1).payeeId(WORKER_1)
                .amount(new BigDecimal("500.00"))
                .commission(new BigDecimal("10.00")).commissionRate(new BigDecimal("0.0200"))
                .tax(new BigDecimal("1.80")).taxRate(new BigDecimal("0.1800"))
                .tip(BigDecimal.ZERO).workerPayout(new BigDecimal("488.20"))
                .method(PaymentMethod.CASH).status(PaymentStatus.COMPLETED)
                .invoiceNumber("HLP-INV-2026-000001")
                .processedAt(LocalDateTime.now().minusDays(5))
                .build());

        // Payment 2: Completed with tip - Electrician task ₹800 + ₹100 tip
        // Commission: 800 * 0.02 = 16, GST: 16 * 0.18 = 2.88, Payout: 800 - 16 - 2.88 + 100 = 881.12
        Payment p2 = paymentRepo.save(Payment.builder()
                .taskId(TASK_2).payerId(CUSTOMER_1).payeeId(WORKER_1)
                .amount(new BigDecimal("800.00"))
                .commission(new BigDecimal("16.00")).commissionRate(new BigDecimal("0.0200"))
                .tax(new BigDecimal("2.88")).taxRate(new BigDecimal("0.1800"))
                .tip(new BigDecimal("100.00")).workerPayout(new BigDecimal("881.12"))
                .method(PaymentMethod.CASH).status(PaymentStatus.COMPLETED)
                .invoiceNumber("HLP-INV-2026-000002")
                .processedAt(LocalDateTime.now().minusDays(2))
                .build());

        // Payment 3: Pending cash payment - Delivery task ₹200
        // Commission: 200 * 0.02 = 4, GST: 4 * 0.18 = 0.72, Payout: 200 - 4 - 0.72 = 195.28
        Payment p3 = paymentRepo.save(Payment.builder()
                .taskId(TASK_3).payerId(CUSTOMER_1).payeeId(WORKER_2)
                .amount(new BigDecimal("200.00"))
                .commission(new BigDecimal("4.00")).commissionRate(new BigDecimal("0.0200"))
                .tax(new BigDecimal("0.72")).taxRate(new BigDecimal("0.1800"))
                .tip(BigDecimal.ZERO).workerPayout(new BigDecimal("195.28"))
                .method(PaymentMethod.CASH).status(PaymentStatus.PENDING)
                .invoiceNumber("HLP-INV-2026-000003")
                .build());

        // Ledger entries for Worker 1
        ledgerRepo.save(WorkerLedgerEntry.builder()
                .workerId(WORKER_1).paymentId(p1.getPaymentId())
                .type(LedgerEntryType.COMMISSION_DUE)
                .amount(new BigDecimal("11.80")).balanceAfter(new BigDecimal("11.80"))
                .description("Commission + GST for plumbing task " + TASK_1).build());

        ledgerRepo.save(WorkerLedgerEntry.builder()
                .workerId(WORKER_1).paymentId(p2.getPaymentId())
                .type(LedgerEntryType.COMMISSION_DUE)
                .amount(new BigDecimal("18.88")).balanceAfter(new BigDecimal("30.68"))
                .description("Commission + GST for electrician task " + TASK_2).build());

        // Ledger entry for Worker 2
        ledgerRepo.save(WorkerLedgerEntry.builder()
                .workerId(WORKER_2).paymentId(p3.getPaymentId())
                .type(LedgerEntryType.COMMISSION_DUE)
                .amount(new BigDecimal("4.72")).balanceAfter(new BigDecimal("4.72"))
                .description("Commission + GST for delivery task " + TASK_3).build());

        log.info("============================================");
        log.info("  Payment Service - Sample data created:");
        log.info("  - 3 Platform config entries (2% commission, 18% GST, 10% cancel fee)");
        log.info("  - 3 Payments (2 completed, 1 pending)");
        log.info("  - Payment 1: ₹500 plumbing, commission ₹10+₹1.80 GST");
        log.info("  - Payment 2: ₹800 electrician + ₹100 tip, payout ₹881.12");
        log.info("  - Payment 3: ₹200 delivery (pending)");
        log.info("  - 3 Ledger entries (Worker 1 owes ₹30.68, Worker 2 owes ₹4.72)");
        log.info("============================================");
    }
}
