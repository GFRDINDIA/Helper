package com.helper.task.controller;

import com.helper.task.dto.request.CreateBidRequest;
import com.helper.task.dto.response.ApiResponse;
import com.helper.task.dto.response.BidResponse;
import com.helper.task.security.AuthenticatedUser;
import com.helper.task.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bidding", description = "Open bidding system: workers bid, customers select")
@SecurityRequirement(name = "bearerAuth")
public class BidController {

    private final BidService bidService;

    @PostMapping("/api/v1/tasks/{taskId}/bids")
    @Operation(summary = "Submit a bid", description = "Workers submit a bid with proposed price and message on OPEN/BIDDING tasks")
    public ResponseEntity<ApiResponse<BidResponse>> createBid(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateBidRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BidResponse bid = bidService.createBid(taskId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid submitted successfully", bid));
    }

    @GetMapping("/api/v1/tasks/{taskId}/bids")
    @Operation(summary = "Get bids for a task", description = "Task owner sees all bids; workers see only their own bid")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBidsForTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<BidResponse> bids = bidService.getBidsForTask(taskId, user);
        return ResponseEntity.ok(ApiResponse.success("Bids retrieved", bids));
    }

    @PutMapping("/api/v1/bids/{bidId}/accept")
    @Operation(summary = "Accept a bid", description = "Task owner accepts a bid. Worker is assigned, price is locked, other bids are rejected.")
    public ResponseEntity<ApiResponse<BidResponse>> acceptBid(
            @PathVariable UUID bidId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BidResponse bid = bidService.acceptBid(bidId, user);
        return ResponseEntity.ok(ApiResponse.success("Bid accepted. Worker assigned to task.", bid));
    }

    @PutMapping("/api/v1/bids/{bidId}/reject")
    @Operation(summary = "Reject a bid", description = "Task owner rejects a specific bid")
    public ResponseEntity<ApiResponse<BidResponse>> rejectBid(
            @PathVariable UUID bidId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BidResponse bid = bidService.rejectBid(bidId, user);
        return ResponseEntity.ok(ApiResponse.success("Bid rejected", bid));
    }

    @DeleteMapping("/api/v1/bids/{bidId}")
    @Operation(summary = "Withdraw a bid", description = "Worker withdraws their own pending bid")
    public ResponseEntity<ApiResponse<BidResponse>> withdrawBid(
            @PathVariable UUID bidId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        BidResponse bid = bidService.withdrawBid(bidId, user);
        return ResponseEntity.ok(ApiResponse.success("Bid withdrawn", bid));
    }

    @GetMapping("/api/v1/bids/my-bids")
    @Operation(summary = "Get my bids", description = "Workers see all bids they have submitted across tasks")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids(
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<BidResponse> bids = bidService.getMyBids(user);
        return ResponseEntity.ok(ApiResponse.success("Your bids retrieved", bids));
    }
}
