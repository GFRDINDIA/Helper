package com.helper.rating.exception;

import com.helper.rating.dto.response.ApiResponse;
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

    @ExceptionHandler(RatingExceptions.RatingNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(RatingExceptions.RatingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "RATING_NOT_FOUND"));
    }

    @ExceptionHandler(RatingExceptions.DuplicateRatingException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(RatingExceptions.DuplicateRatingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage(), "DUPLICATE_RATING"));
    }

    @ExceptionHandler(RatingExceptions.InvalidRatingException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalid(RatingExceptions.InvalidRatingException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "INVALID_RATING"));
    }

    @ExceptionHandler(RatingExceptions.UnauthorizedRatingException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorized(RatingExceptions.UnauthorizedRatingException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), "FORBIDDEN"));
    }

    @ExceptionHandler(RatingExceptions.RatingWindowExpiredException.class)
    public ResponseEntity<ApiResponse<?>> handleExpired(RatingExceptions.RatingWindowExpiredException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage(), "RATING_WINDOW_EXPIRED"));
    }

    @ExceptionHandler(RatingExceptions.FlagNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleFlagNotFound(RatingExceptions.FlagNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage(), "FLAG_NOT_FOUND"));
    }

    @ExceptionHandler(RatingExceptions.DuplicateFlagException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateFlag(RatingExceptions.DuplicateFlagException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage(), "DUPLICATE_FLAG"));
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
