package com.helper.payment;

import com.helper.payment.service.PaymentCalculator;
import com.helper.payment.service.PaymentCalculator.PaymentBreakdown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentCalculator is the financial engine — must have 100% test coverage.
 * Tests the formula from PRD Section 3.5:
 *   commission = amount * commissionRate
 *   tax = commission * gstRate
 *   workerPayout = amount - commission - tax + tip
 *   customerTotal = amount + tip
 */
class PaymentCalculatorTest {

    private static final BigDecimal RATE_2 = new BigDecimal("0.02");   // 2% commission
    private static final BigDecimal GST_18 = new BigDecimal("0.18");   // 18% GST

    @Test
    @DisplayName("PRD example: ₹1000 task + ₹100 tip")
    void testPrdExample() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("1000"), new BigDecimal("100"), RATE_2, GST_18);

        assertEquals(new BigDecimal("1000.00"), b.getAmount());
        assertEquals(new BigDecimal("20.00"), b.getCommission());     // 1000 * 0.02
        assertEquals(new BigDecimal("3.60"), b.getTax());              // 20 * 0.18
        assertEquals(new BigDecimal("100.00"), b.getTip());
        assertEquals(new BigDecimal("1076.40"), b.getWorkerPayout()); // 1000 - 20 - 3.60 + 100
        assertEquals(new BigDecimal("1100.00"), b.getCustomerTotal()); // 1000 + 100
        assertEquals(new BigDecimal("20.00"), b.getPlatformRevenue());
        assertEquals(new BigDecimal("3.60"), b.getPlatformTaxLiability());
        assertEquals(new BigDecimal("23.60"), b.getTotalDeduction());  // 20 + 3.60
    }

    @Test
    @DisplayName("Zero tip")
    void testZeroTip() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("500"), BigDecimal.ZERO, RATE_2, GST_18);

        assertEquals(new BigDecimal("500.00"), b.getAmount());
        assertEquals(new BigDecimal("10.00"), b.getCommission());
        assertEquals(new BigDecimal("1.80"), b.getTax());
        assertEquals(new BigDecimal("0.00"), b.getTip());
        assertEquals(new BigDecimal("488.20"), b.getWorkerPayout()); // 500 - 10 - 1.80
        assertEquals(new BigDecimal("500.00"), b.getCustomerTotal());
    }

    @Test
    @DisplayName("Null tip treated as zero")
    void testNullTip() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("800"), null, RATE_2, GST_18);

        assertEquals(new BigDecimal("0.00"), b.getTip());
        assertEquals(new BigDecimal("781.12"), b.getWorkerPayout()); // 800 - 16 - 2.88
    }

    @Test
    @DisplayName("Zero commission rate")
    void testZeroCommission() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("1000"), new BigDecimal("50"), BigDecimal.ZERO, GST_18);

        assertEquals(new BigDecimal("0.00"), b.getCommission());
        assertEquals(new BigDecimal("0.00"), b.getTax());
        assertEquals(new BigDecimal("1050.00"), b.getWorkerPayout()); // 1000 + 50, no deduction
    }

    @Test
    @DisplayName("High commission rate (5%)")
    void testHighCommission() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("0.05"), GST_18);

        assertEquals(new BigDecimal("50.00"), b.getCommission());     // 1000 * 0.05
        assertEquals(new BigDecimal("9.00"), b.getTax());              // 50 * 0.18
        assertEquals(new BigDecimal("941.00"), b.getWorkerPayout());  // 1000 - 50 - 9
    }

    @Test
    @DisplayName("Very small amount (₹1)")
    void testSmallAmount() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("1"), BigDecimal.ZERO, RATE_2, GST_18);

        assertEquals(new BigDecimal("1.00"), b.getAmount());
        assertEquals(new BigDecimal("0.02"), b.getCommission());  // 1 * 0.02
        assertEquals(new BigDecimal("0.00"), b.getTax());          // 0.02 * 0.18 = 0.0036 -> 0.00
        assertEquals(new BigDecimal("0.98"), b.getWorkerPayout());
    }

    @Test
    @DisplayName("Large amount (₹100,000)")
    void testLargeAmount() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("100000"), new BigDecimal("5000"), RATE_2, GST_18);

        assertEquals(new BigDecimal("2000.00"), b.getCommission());   // 100000 * 0.02
        assertEquals(new BigDecimal("360.00"), b.getTax());            // 2000 * 0.18
        assertEquals(new BigDecimal("102640.00"), b.getWorkerPayout()); // 100000 - 2000 - 360 + 5000
        assertEquals(new BigDecimal("105000.00"), b.getCustomerTotal());
    }

    @Test
    @DisplayName("Rounding precision - odd amounts")
    void testRoundingPrecision() {
        PaymentBreakdown b = PaymentCalculator.calculate(
                new BigDecimal("333.33"), BigDecimal.ZERO, RATE_2, GST_18);

        assertEquals(new BigDecimal("333.33"), b.getAmount());
        assertEquals(new BigDecimal("6.67"), b.getCommission());   // 333.33 * 0.02 = 6.6666 → 6.67
        assertEquals(new BigDecimal("1.20"), b.getTax());           // 6.67 * 0.18 = 1.2006 → 1.20
        assertEquals(new BigDecimal("325.46"), b.getWorkerPayout()); // 333.33 - 6.67 - 1.20
    }

    @Test
    @DisplayName("Null final price throws exception")
    void testNullPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentCalculator.calculate(null, BigDecimal.ZERO, RATE_2, GST_18));
    }

    @Test
    @DisplayName("Zero final price throws exception")
    void testZeroPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO, RATE_2, GST_18));
    }

    @Test
    @DisplayName("Negative final price throws exception")
    void testNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentCalculator.calculate(new BigDecimal("-100"), BigDecimal.ZERO, RATE_2, GST_18));
    }

    @Test
    @DisplayName("Negative tip throws exception")
    void testNegativeTip() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentCalculator.calculate(new BigDecimal("100"), new BigDecimal("-50"), RATE_2, GST_18));
    }

    @Test
    @DisplayName("Cancellation fee calculation")
    void testCancellationFee() {
        BigDecimal fee = PaymentCalculator.calculateCancellationFee(
                new BigDecimal("1000"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("100.00"), fee);
    }

    @Test
    @DisplayName("Cancellation fee - small amount rounding")
    void testCancellationFeeRounding() {
        BigDecimal fee = PaymentCalculator.calculateCancellationFee(
                new BigDecimal("333.33"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("33.33"), fee);
    }
}
