package com.helper.notification.exception;

import com.helper.notification.dto.response.ApiResponse;
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

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> errors.put(((FieldError)e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder().success(false).message("Validation failed").data(errors).error("VALIDATION_ERROR").build());
    }
    @ExceptionHandler(NotificationExceptions.NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(RuntimeException ex) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "NOT_FOUND")); }
    @ExceptionHandler(NotificationExceptions.UnauthorizedNotificationException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(RuntimeException ex) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), "FORBIDDEN")); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccess(AccessDeniedException ex) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied", "FORBIDDEN")); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) { log.error("Unexpected: ", ex); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal error", "INTERNAL_ERROR")); }
}
