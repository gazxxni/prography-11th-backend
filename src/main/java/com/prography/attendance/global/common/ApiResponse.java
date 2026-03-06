package com.prography.attendance.global.common;

public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorBody getError() {
        return error;
    }

    public record ErrorBody(String code, String message) {
    }
}
