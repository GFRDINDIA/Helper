package com.helper.user.controller;

import com.helper.user.dto.request.KycDocumentRequest;
import com.helper.user.dto.request.KycReviewRequest;
import com.helper.user.dto.response.ApiResponse;
import com.helper.user.dto.response.KycDocumentResponse;
import com.helper.user.security.AuthenticatedUser;
import com.helper.user.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycService kycService;

    // ===== WORKER KYC ENDPOINTS =====

    @PostMapping("/api/v1/workers/kyc")
    @Tag(name = "KYC Verification", description = "Worker KYC document submission and admin approval workflow")
    @Operation(summary = "Submit KYC documents",
            description = "Workers upload identity documents based on their domain's KYC level. " +
                    "BASIC (Delivery/Farming): Aadhaar + PAN + Selfie. " +
                    "PROFESSIONAL (Electrician/Plumbing): + Professional License. " +
                    "PROFESSIONAL_PLUS_LICENSE (Medical/Finance/Education): + Regulatory License.")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> submitKyc(
            @Valid @RequestBody List<KycDocumentRequest> documents,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("KYC documents submitted for review",
                        kycService.submitKycDocuments(documents, user)));
    }

    @GetMapping("/api/v1/workers/kyc")
    @Tag(name = "KYC Verification")
    @Operation(summary = "View my KYC status", description = "Workers view their submitted documents and review status")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getMyKyc(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success("Your KYC documents",
                kycService.getMyKycDocuments(user)));
    }

    // ===== ADMIN KYC ENDPOINTS =====

    @GetMapping("/api/v1/admin/kyc/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Tag(name = "Admin - KYC Management")
    @Operation(summary = "Get pending KYC queue", description = "List all documents awaiting admin review")
    public ResponseEntity<ApiResponse<List<KycDocumentResponse>>> getPendingKyc() {
        return ResponseEntity.ok(ApiResponse.success("Pending KYC documents",
                kycService.getPendingKycDocuments()));
    }

    @PutMapping("/api/v1/admin/kyc/{documentId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Tag(name = "Admin - KYC Management")
    @Operation(summary = "Review a KYC document",
            description = "Approve or reject a document with comments. " +
                    "When all documents for a worker are approved, the worker gets VERIFIED status.")
    public ResponseEntity<ApiResponse<KycDocumentResponse>> reviewKyc(
            @PathVariable UUID documentId,
            @Valid @RequestBody KycReviewRequest request,
            @AuthenticationPrincipal AuthenticatedUser admin) {
        return ResponseEntity.ok(ApiResponse.success(
                "Document " + request.getStatus().name().toLowerCase(),
                kycService.reviewKycDocument(documentId, request, admin)));
    }

    @GetMapping("/api/v1/admin/kyc/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Tag(name = "Admin - KYC Management")
    @Operation(summary = "KYC statistics", description = "Count of documents by status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getKycStats() {
        return ResponseEntity.ok(ApiResponse.success("KYC statistics", kycService.getKycStats()));
    }
}
