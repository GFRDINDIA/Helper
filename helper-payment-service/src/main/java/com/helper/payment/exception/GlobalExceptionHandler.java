package com.helper.payment.exception;

import com.helper.payment.dto.response.ApiResponse;
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
                ApiResponse.<Map<String, String>>builder().success(false).message("Validation failed")
                        .data(errors).error("VALIDATION_ERROR").build());
    }

    @ExceptionHandler(PaymentExceptions.PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(PaymentExceptions.PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "PAYMENT_NOT_FOUND"));
    }

    @ExceptionHandler(PaymentExceptions.InvalidPaymentStateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidState(PaymentExceptions.InvalidPaymentStateException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_STATE"));
    }

    @ExceptionHandler(PaymentExceptions.UnauthorizedPaymentAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(PaymentExceptions.UnauthorizedPaymentAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(PaymentExceptions.DuplicatePaymentException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(PaymentExceptions.DuplicatePaymentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), "DUPLICATE_PAYMENT"));
    }

    @ExceptionHandler(PaymentExceptions.InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficient(PaymentExceptions.InsufficientFundsException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INSUFFICIENT_FUNDS"));
    }

    @ExceptionHandler(PaymentExceptions.ConfigNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleConfigNotFound(PaymentExceptions.ConfigNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "CONFIG_NOT_FOUND"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "FORBIDDEN"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
