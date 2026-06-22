package com.bit.portal.global.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private static final String DEFAULT_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    private final boolean success = true;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    private ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /** data 포함, 기본 메시지 */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, DEFAULT_MESSAGE);
    }

    /** data 포함, 커스텀 메시지 */
    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    /** data 없음, 기본 메시지 */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(null, DEFAULT_MESSAGE);
    }

    /** data 없음, 커스텀 메시지 */
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(null, message);
    }
}
