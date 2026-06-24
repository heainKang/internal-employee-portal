package com.bit.portal.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 공통 (C) ────────────────────────────────────────────────────────────
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // ── 인증/보안 (A) ─────────────────────────────────────────────────────────
    // 인증 실패 원인은 모두 동일 메시지로 통일 (보안)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    LOGIN_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "A003", "로그인 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요."),

    // ── 직원 (E) ──────────────────────────────────────────────────────────────
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "직원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "E002", "이미 사용 중인 이메일입니다."),
    ALREADY_RESIGNED(HttpStatus.BAD_REQUEST, "E003", "이미 퇴사 처리된 직원입니다."),

    // ── 외부 API (X) ──────────────────────────────────────────────────────────
    EXTERNAL_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "X001", "외부 서비스를 현재 사용할 수 없습니다. 잠시 후 다시 시도해주세요."),
    EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "X002", "외부 서비스 응답 시간이 초과되었습니다."),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "X003", "외부 서비스에서 오류가 발생했습니다."),
    BACKGROUND_CHECK_NOT_FOUND(HttpStatus.NOT_FOUND, "X004", "배경 조회 결과를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
