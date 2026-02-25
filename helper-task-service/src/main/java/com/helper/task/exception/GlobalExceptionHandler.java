package com.helper.task.exception;

import com.helper.task.dto.response.ApiResponse;
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
        ex.getBindingResult().getAllErrors().forEach(e -> {
            errors.put(((FieldError) e).getField(), e.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(
                ApiResponse.<Map<String, String>>builder()
                        .success(false).message("Validation failed").data(errors).error("VALIDATION_ERROR").build());
    }

    @ExceptionHandler(TaskExceptions.TaskNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleTaskNotFound(TaskExceptions.TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "TASK_NOT_FOUND"));
    }

    @ExceptionHandler(TaskExceptions.BidNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleBidNotFound(TaskExceptions.BidNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "BID_NOT_FOUND"));
    }

    @ExceptionHandler(TaskExceptions.InvalidTaskStateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidState(TaskExceptions.InvalidTaskStateException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_STATE"));
    }

    @ExceptionHandler(TaskExceptions.InvalidBidException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidBid(TaskExceptions.InvalidBidException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_BID"));
    }

    @ExceptionHandler(TaskExceptions.UnauthorizedTaskAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(TaskExceptions.UnauthorizedTaskAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(TaskExceptions.DuplicateBidException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(TaskExceptions.DuplicateBidException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage(), "DUPLICATE_BID"));
    }

    @ExceptionHandler(TaskExceptions.BidLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleBidLimit(TaskExceptions.BidLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(ex.getMessage(), "BID_LIMIT"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied", "FORBIDDEN"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
