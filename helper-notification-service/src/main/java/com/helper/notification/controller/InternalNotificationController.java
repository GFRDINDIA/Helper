package com.helper.notification.controller;

import com.helper.notification.dto.response.ApiResponse;
import com.helper.notification.dto.response.NotificationResponse;
import com.helper.notification.enums.NotificationEvent;
import com.helper.notification.enums.NotificationPriority;
import com.helper.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Lightweight endpoints called by other Helper microservices
 * to trigger lifecycle notifications without building full request objects.
 */
@RestController
@RequestMapping("/api/v1/internal/notify")
@RequiredArgsConstructor
@Tag(name = "Internal - Service-to-Service", description = "Called by Task, Payment, Rating services to trigger notifications")
@SecurityRequirement(name = "bearerAuth")
public class InternalNotificationController {

    private final NotificationService notifService;

    @PostMapping("/task-status")
    @Operation(summary = "Notify task status change",
            description = "Called by Task Service when task moves to new status")
    public ResponseEntity<ApiResponse<NotificationResponse>> taskStatusChange(
            @RequestParam UUID userId,
            @RequestParam UUID taskId,
            @RequestParam String taskTitle,
            @RequestParam String newStatus) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(userId, NotificationEvent.TASK_STATUS_CHANGE,
                        "Task Update: " + taskTitle,
                        "Your task \"" + taskTitle + "\" is now " + newStatus + ".",
                        Map.of("taskId", taskId.toString(), "status", newStatus))));
    }

    @PostMapping("/payment-received")
    @Operation(summary = "Notify worker of payment received")
    public ResponseEntity<ApiResponse<NotificationResponse>> paymentReceived(
            @RequestParam UUID workerId,
            @RequestParam UUID taskId,
            @RequestParam String amount) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(workerId, NotificationEvent.PAYMENT_RECEIVED,
                        "Payment Received: ₹" + amount,
                        "You received ₹" + amount + " for your completed task.",
                        Map.of("taskId", taskId.toString(), "amount", amount))));
    }

    @PostMapping("/new-bid")
    @Operation(summary = "Notify customer of new bid on their task")
    public ResponseEntity<ApiResponse<NotificationResponse>> newBidReceived(
            @RequestParam UUID customerId,
            @RequestParam UUID taskId,
            @RequestParam String workerName,
            @RequestParam String bidAmount) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(customerId, NotificationEvent.NEW_BID_RECEIVED,
                        "New Bid: ₹" + bidAmount + " from " + workerName,
                        workerName + " bid ₹" + bidAmount + " on your task.",
                        Map.of("taskId", taskId.toString(), "bidAmount", bidAmount))));
    }

    @PostMapping("/bid-accepted")
    @Operation(summary = "Notify worker their bid was accepted")
    public ResponseEntity<ApiResponse<NotificationResponse>> bidAccepted(
            @RequestParam UUID workerId,
            @RequestParam UUID taskId,
            @RequestParam String taskTitle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(workerId, NotificationEvent.BID_ACCEPTED,
                        "Bid Accepted!",
                        "Your bid on \"" + taskTitle + "\" has been accepted.",
                        Map.of("taskId", taskId.toString()))));
    }

    @PostMapping("/rating-received")
    @Operation(summary = "Notify user they received a rating")
    public ResponseEntity<ApiResponse<NotificationResponse>> ratingReceived(
            @RequestParam UUID userId,
            @RequestParam UUID taskId,
            @RequestParam int score) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(userId, NotificationEvent.RATING_RECEIVED,
                        "New " + score + "-Star Rating",
                        "You received a " + score + "-star rating for a completed task.",
                        Map.of("taskId", taskId.toString(), "score", String.valueOf(score)))));
    }

    @PostMapping("/kyc-status")
    @Operation(summary = "Notify worker of KYC status update")
    public ResponseEntity<ApiResponse<NotificationResponse>> kycStatus(
            @RequestParam UUID workerId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        NotificationEvent event = "APPROVED".equalsIgnoreCase(status)
                ? NotificationEvent.KYC_APPROVED : NotificationEvent.KYC_REJECTED;
        String title = "APPROVED".equalsIgnoreCase(status) ? "KYC Approved!" : "KYC Rejected";
        String body = "APPROVED".equalsIgnoreCase(status)
                ? "Congratulations! Your KYC verification is complete. You can now accept tasks."
                : "Your KYC was rejected" + (reason != null ? ": " + reason : "") + ". Please resubmit.";
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sent",
                notifService.sendToUser(workerId, event, title, body,
                        Map.of("kycStatus", status))));
    }
}
