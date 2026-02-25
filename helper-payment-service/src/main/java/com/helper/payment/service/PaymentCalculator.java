package com.helper.payment.service;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * THE most critical class in the Payment Service.
 * Pure utility — no DB, no Spring — takes inputs, returns calculated breakdown.
 *
 * Formula (from PRD Section 3.5):
 *   commission = finalPrice * commissionRate
 *   tax        = commission * gstRate
 *   workerPayout = finalPrice - commission - tax + tip
 *   customerTotal = finalPrice + tip
 *   platformRevenue = commission
 *   platformTaxLiability = tax
 *
 * Rules:
 *   - All calculations use BigDecimal with HALF_UP rounding, scale 2
 *   - Tip is 100% to worker, ZERO commission on tips
 *   - Commission is deducted from worker payout, NOT added to customer bill
 */
public class PaymentCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    /**
     * Calculate complete payment breakdown.
     *
     * @param finalPrice     Task final price (must be > 0)
     * @param tipAmount      Tip amount (>= 0, null treated as 0)
     * @param commissionRate Commission rate (e.g. 0.02 for 2%)
     * @param gstRate        GST rate on commission (e.g. 0.18 for 18%)
     * @return PaymentBreakdown with all calculated values
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static PaymentBreakdown calculate(
            BigDecimal finalPrice,
            BigDecimal tipAmount,
            BigDecimal commissionRate,
            BigDecimal gstRate) {

        // Validate inputs
        if (finalPrice == null) throw new IllegalArgumentException("Final price cannot be null");
        if (commissionRate == null) throw new IllegalArgumentException("Commission rate cannot be null");
        if (gstRate == null) throw new IllegalArgumentException("GST rate cannot be null");
        if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Final price must be positive");
        if (commissionRate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Commission rate cannot be negative");
        if (gstRate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("GST rate cannot be negative");

        BigDecimal tip = (tipAmount != null && tipAmount.compareTo(BigDecimal.ZERO) >= 0)
                ? tipAmount.setScale(SCALE, RM) : BigDecimal.ZERO.setScale(SCALE, RM);

        if (tipAmount != null && tipAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tip amount cannot be negative");
        }

        // Calculate
        BigDecimal amount = finalPrice.setScale(SCALE, RM);
        BigDecimal commission = amount.multiply(commissionRate).setScale(SCALE, RM);
        BigDecimal tax = commission.multiply(gstRate).setScale(SCALE, RM);
        BigDecimal totalDeduction = commission.add(tax);
        BigDecimal workerBasePayout = amount.subtract(totalDeduction);
        BigDecimal workerPayout = workerBasePayout.add(tip);
        BigDecimal customerTotal = amount.add(tip);

        return PaymentBreakdown.builder()
                .amount(amount)
                .commission(commission)
                .commissionRate(commissionRate)
                .tax(tax)
                .taxRate(gstRate)
                .tip(tip)
                .totalDeduction(totalDeduction)
                .workerPayout(workerPayout)
                .customerTotal(customerTotal)
                .platformRevenue(commission)
                .platformTaxLiability(tax)
                .build();
    }

    /**
     * Calculate cancellation fee.
     */
    public static BigDecimal calculateCancellationFee(BigDecimal agreedPrice, BigDecimal cancellationRate) {
        if (agreedPrice == null || cancellationRate == null) throw new IllegalArgumentException("Arguments cannot be null");
        if (agreedPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Agreed price must be positive");
        return agreedPrice.multiply(cancellationRate).setScale(SCALE, RM);
    }

    @Getter @Builder
    public static class PaymentBreakdown {
        private final BigDecimal amount;            // Task final price
        private final BigDecimal commission;         // Platform commission
        private final BigDecimal commissionRate;     // Rate used
        private final BigDecimal tax;                // GST on commission
        private final BigDecimal taxRate;            // GST rate used
        private final BigDecimal tip;                // Customer tip
        private final BigDecimal totalDeduction;     // commission + tax
        private final BigDecimal workerPayout;       // amount - deduction + tip
        private final BigDecimal customerTotal;      // amount + tip
        private final BigDecimal platformRevenue;    // commission
        private final BigDecimal platformTaxLiability; // tax
    }
}
