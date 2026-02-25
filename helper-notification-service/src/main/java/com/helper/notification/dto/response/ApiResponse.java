package com.helper.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    @Builder.Default private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(String msg, T data) { return ApiResponse.<T>builder().success(true).message(msg).data(data).build(); }
    public static <T> ApiResponse<T> success(String msg) { return ApiResponse.<T>builder().success(true).message(msg).build(); }
    public static <T> ApiResponse<T> error(String msg, String err) { return ApiResponse.<T>builder().success(false).message(msg).error(err).build(); }
}
