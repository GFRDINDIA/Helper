package com.helper.payment.service;

import com.helper.payment.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates PDF invoices for each payment.
 * MVP: HTML → PDF using openhtmltopdf.
 * Production: Store on S3 and return URL.
 */
@Service
@Slf4j
public class InvoiceService {

    @Value("${app.upload.invoices-dir:uploads/invoices}")
    private String invoicesDir;

    @Value("${app.upload.storage-type:local}")
    private String storageType;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final String COMPANY_NAME = "Grace and Faith Research and Development Pvt Ltd";
    private static final String COMPANY_GSTIN = "XXXXXXXXXXXXXXXXX"; // Replace with actual GSTIN
    private static final String PLATFORM_NAME = "Helper";

    /**
     * Generate invoice PDF for a payment and return the file path/URL.
     */
    public String generateInvoice(Payment payment) {
        try {
            // Ensure directory exists
            File dir = new File(invoicesDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = payment.getInvoiceNumber().replace("/", "-") + ".pdf";
            String filePath = invoicesDir + File.separator + fileName;

            // Generate HTML
            String html = buildInvoiceHtml(payment);

            // Convert HTML to PDF using openhtmltopdf
            try (OutputStream os = new FileOutputStream(filePath)) {
                com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder =
                        new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }

            log.info("Invoice generated: {} at {}", payment.getInvoiceNumber(), filePath);

            // In production, upload to S3 and return URL
            if ("s3".equals(storageType)) {
                // TODO: Upload to S3 and return public URL
                return "https://s3.amazonaws.com/helper-uploads/invoices/" + fileName;
            }

            return filePath;

        } catch (Exception e) {
            log.error("Failed to generate invoice for payment {}: {}", payment.getPaymentId(), e.getMessage(), e);
            throw new RuntimeException("Invoice generation failed: " + e.getMessage(), e);
        }
    }

    private String buildInvoiceHtml(Payment payment) {
        BigDecimal customerTotal = payment.getAmount().add(payment.getTip());
        String dateStr = payment.getCreatedAt() != null
                ? payment.getCreatedAt().format(DATE_FMT) : LocalDateTime.now().format(DATE_FMT);

        return """
            <!DOCTYPE html>
            <html>
            <head>
            <style>
                body { font-family: Arial, sans-serif; margin: 40px; color: #2C3E50; font-size: 13px; }
                .header { display: flex; justify-content: space-between; border-bottom: 3px solid #1A5276; padding-bottom: 15px; margin-bottom: 20px; }
                .brand { font-size: 28px; font-weight: bold; color: #1A5276; }
                .brand-sub { font-size: 11px; color: #7F8C8D; }
                .invoice-title { text-align: right; }
                .invoice-title h2 { margin: 0; color: #1A5276; font-size: 22px; }
                .invoice-title p { margin: 2px 0; color: #7F8C8D; }
                .parties { display: flex; justify-content: space-between; margin: 20px 0; }
                .party { width: 45%%; }
                .party h4 { color: #1A5276; margin-bottom: 5px; font-size: 13px; border-bottom: 1px solid #BDC3C7; padding-bottom: 3px; }
                .party p { margin: 2px 0; }
                table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                th { background: #1A5276; color: white; padding: 10px 12px; text-align: left; font-size: 12px; }
                td { padding: 8px 12px; border-bottom: 1px solid #EAECEE; }
                tr:nth-child(even) { background: #F8F9F9; }
                .total-row td { font-weight: bold; border-top: 2px solid #1A5276; font-size: 14px; }
                .highlight { background: #EBF5FB !important; }
                .green { color: #27AE60; }
                .amount { text-align: right; font-family: 'Courier New', monospace; }
                .footer { margin-top: 30px; border-top: 1px solid #BDC3C7; padding-top: 10px; font-size: 11px; color: #7F8C8D; }
                .gst-note { background: #FEF5E7; padding: 10px; border-radius: 4px; margin: 15px 0; font-size: 11px; }
            </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="brand">%s</div>
                        <div class="brand-sub">%s</div>
                        <div class="brand-sub">GSTIN: %s</div>
                    </div>
                    <div class="invoice-title">
                        <h2>INVOICE</h2>
                        <p><strong>%s</strong></p>
                        <p>Date: %s</p>
                        <p>Status: %s</p>
                    </div>
                </div>

                <div class="parties">
                    <div class="party">
                        <h4>BILLED TO (Customer)</h4>
                        <p>Customer ID: %s</p>
                        <p>Payment Method: %s</p>
                    </div>
                    <div class="party">
                        <h4>SERVICE PROVIDED BY (Worker)</h4>
                        <p>Worker ID: %s</p>
                        <p>Task ID: %s</p>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr><th>Description</th><th>Rate</th><th style="text-align:right">Amount (INR)</th></tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Task Service Fee</td>
                            <td>-</td>
                            <td class="amount">%s</td>
                        </tr>
                        <tr>
                            <td>Platform Commission (%s%%)</td>
                            <td>%s%% of %s</td>
                            <td class="amount">-%s</td>
                        </tr>
                        <tr>
                            <td>GST on Commission (%s%%)</td>
                            <td>%s%% of %s</td>
                            <td class="amount">-%s</td>
                        </tr>
                        %s
                        <tr class="total-row">
                            <td colspan="2">Customer Total Paid</td>
                            <td class="amount">%s</td>
                        </tr>
                        <tr class="total-row highlight">
                            <td colspan="2" class="green">Worker Net Payout</td>
                            <td class="amount green">%s</td>
                        </tr>
                    </tbody>
                </table>

                <div class="gst-note">
                    <strong>GST Note:</strong> GST of %s%% is applied only on the platform commission of %s%%, not on the total task amount.
                    Platform commission: ₹%s | GST on commission: ₹%s | Total platform deduction: ₹%s
                </div>

                <div class="footer">
                    <p>This is a computer-generated invoice and does not require a signature.</p>
                    <p>%s | %s</p>
                    <p>For queries, contact support@helper.app</p>
                </div>
            </body>
            </html>
            """.formatted(
                PLATFORM_NAME, COMPANY_NAME, COMPANY_GSTIN,
                payment.getInvoiceNumber(), dateStr, payment.getStatus().name(),
                shortenUuid(payment.getPayerId()), payment.getMethod().name(),
                shortenUuid(payment.getPayeeId()), shortenUuid(payment.getTaskId()),
                formatMoney(payment.getAmount()),
                formatPercent(payment.getCommissionRate()), formatPercent(payment.getCommissionRate()),
                formatMoney(payment.getAmount()), formatMoney(payment.getCommission()),
                formatPercent(payment.getTaxRate()), formatPercent(payment.getTaxRate()),
                formatMoney(payment.getCommission()), formatMoney(payment.getTax()),
                payment.getTip().compareTo(BigDecimal.ZERO) > 0
                    ? "<tr class=\"highlight\"><td>Tip (100%% to worker)</td><td>-</td><td class=\"amount green\">+" + formatMoney(payment.getTip()) + "</td></tr>"
                    : "",
                formatMoney(customerTotal),
                formatMoney(payment.getWorkerPayout()),
                formatPercent(payment.getTaxRate()), formatPercent(payment.getCommissionRate()),
                formatMoney(payment.getCommission()), formatMoney(payment.getTax()),
                formatMoney(payment.getCommission().add(payment.getTax())),
                COMPANY_NAME, payment.getInvoiceNumber()
        );
    }

    private String formatMoney(BigDecimal amount) {
        return String.format("₹%,.2f", amount);
    }

    private String formatPercent(BigDecimal rate) {
        return rate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
    }

    private String shortenUuid(java.util.UUID uuid) {
        return uuid != null ? uuid.toString().substring(0, 8) + "..." : "N/A";
    }
}
