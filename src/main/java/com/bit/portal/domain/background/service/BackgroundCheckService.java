package com.bit.portal.domain.background.service;

import com.bit.portal.domain.background.client.BackgroundCheckClient;
import com.bit.portal.domain.background.dto.*;
import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.repository.EmployeeRepository;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import com.bit.portal.global.error.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackgroundCheckService {

    private final BackgroundCheckClient backgroundCheckClient;
    private final EmployeeRepository employeeRepository;

    /**
     * 배경 조회 요청 (POST)
     * CB: bgCheck-create — 쓰기 전용, 실패 시 30s 차단
     */
    @CircuitBreaker(name = "bgCheck-create", fallbackMethod = "createCheckFallback")
    public BackgroundCheckCreatedResponse createCheck(BackgroundCheckCreateRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));

        BackgroundCheckApiRequest apiRequest = BackgroundCheckApiRequest.builder()
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .dateOfBirth(employee.getDateOfBirth() != null
                        ? employee.getDateOfBirth().toString() : null)
                .build();

        return backgroundCheckClient.createCheck(apiRequest);
    }

    private BackgroundCheckCreatedResponse createCheckFallback(BackgroundCheckCreateRequest request, Exception e) {
        return handleFallback("bgCheck-create", e);
    }

    /**
     * 배경 조회 단건 결과 (GET /{checkId})
     * CB: bgCheck-get — 읽기 전용, 20s 차단
     */
    @CircuitBreaker(name = "bgCheck-get", fallbackMethod = "getCheckFallback")
    public BackgroundCheckResultResponse getCheck(String checkId) {
        return backgroundCheckClient.getCheck(checkId);
    }

    private BackgroundCheckResultResponse getCheckFallback(String checkId, Exception e) {
        return handleFallback("bgCheck-get", e);
    }

    /**
     * 직원별 배경 조회 이력 (GET ?employeeId=)
     * CB: bgCheck-list — 읽기 전용, 20s 차단
     */
    @CircuitBreaker(name = "bgCheck-list", fallbackMethod = "listChecksFallback")
    public BackgroundCheckListResponse listChecks(String employeeId) {
        return backgroundCheckClient.listChecks(employeeId);
    }

    private BackgroundCheckListResponse listChecksFallback(String employeeId, Exception e) {
        return handleFallback("bgCheck-list", e);
    }

    // ── Fallback 공통 처리 ────────────────────────────────────────────────────

    /**
     * CB가 ignore-exceptions로 설정한 BusinessException은 Fallback을 거치지 않고 직접 전파됨.
     * 이 메서드가 호출되는 경우:
     *   1. ExternalApiException (5xx, timeout, 연결 오류) → CB가 카운트 후 Fallback 호출 → 그대로 re-throw
     *   2. CallNotPermittedException (CB OPEN) → Fallback 호출 → EXTERNAL_API_UNAVAILABLE
     *   3. 그 외 예상치 못한 예외 → EXTERNAL_API_UNAVAILABLE
     */
    private <T> T handleFallback(String ctx, Exception e) {
        if (e instanceof ExternalApiException ee) {
            log.warn("[{}] External API error: code={}, message={}", ctx, ee.getErrorCode().getCode(), ee.getMessage());
            throw ee;
        }
        if (e instanceof CallNotPermittedException) {
            log.warn("[{}] Circuit breaker OPEN — calls not permitted", ctx);
            throw new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
        }
        log.error("[{}] Unexpected fallback exception: {} - {}", ctx, e.getClass().getSimpleName(), e.getMessage());
        throw new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
    }
}
