package com.helper.rating.controller;

import com.helper.rating.dto.request.SubmitFlagRequest;
import com.helper.rating.dto.response.ApiResponse;
import com.helper.rating.dto.response.FlagResponse;
import com.helper.rating.security.AuthenticatedUser;
import com.helper.rating.service.FlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/flags")
@RequiredArgsConstructor
@Tag(name = "Flags", description = "Flag inappropriate behavior or ratings for admin review")
@SecurityRequirement(name = "bearerAuth")
public class FlagController {

    private final FlagService flagService;

    @PostMapping
    @Operation(summary = "Flag a user or rating",
            description = "Either party can flag inappropriate behavior. " +
                    "Provides reason (HARASSMENT, FAKE_REVIEW, etc.) and optional description. " +
                    "Auto-hides rating after threshold flags.")
    public ResponseEntity<ApiResponse<FlagResponse>> submitFlag(
            @Valid @RequestBody SubmitFlagRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flag submitted for admin review",
                        flagService.submitFlag(request, user)));
    }
}
