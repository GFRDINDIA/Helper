package com.helper.user.exception;

import com.helper.user.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e ->
                errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(
                ApiResponse.<Map<String, String>>builder().success(false).message("Validation failed").data(errors).error("VALIDATION_ERROR").build());
    }

    @ExceptionHandler(UserExceptions.ProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(UserExceptions.ProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "PROFILE_NOT_FOUND"));
    }

    @ExceptionHandler(UserExceptions.DocumentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleDocNotFound(UserExceptions.DocumentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "DOCUMENT_NOT_FOUND"));
    }

    @ExceptionHandler(UserExceptions.InvalidProfileException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalid(UserExceptions.InvalidProfileException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_PROFILE"));
    }

    @ExceptionHandler(UserExceptions.InvalidKycException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidKyc(UserExceptions.InvalidKycException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_KYC"));
    }

    @ExceptionHandler(UserExceptions.UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(UserExceptions.UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(UserExceptions.ProfileAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(UserExceptions.ProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage(), "PROFILE_EXISTS"));
    }

    @ExceptionHandler(UserExceptions.DuplicateSkillException.class)
    public ResponseEntity<ApiResponse<?>> handleDupSkill(UserExceptions.DuplicateSkillException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage(), "DUPLICATE_SKILL"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied", "FORBIDDEN"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
