package com.helper.task.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Pagination fields
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder().success(true).message(message).build();
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder().success(false).message(message).error(error).build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

    public static <T> ApiResponse<T> paged(String message, T data, int page, int size, long total, int totalPages) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data)
                .page(page).size(size).totalElements(total).totalPages(totalPages)
                .build();
    }
}
