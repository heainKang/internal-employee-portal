package com.bit.portal.domain.background.service;

import com.bit.portal.domain.background.client.BackgroundCheckClient;
import com.bit.portal.domain.background.dto.*;
import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.repository.EmployeeRepository;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import com.bit.portal.global.error.exception.ExternalApiException;
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
     * 배경 조회 요청
     * CircuitBreaker: 5회 중 50% 실패 시 OPEN (30초 차단) → fallback 즉시 응답
     */
    @CircuitBreaker(name = "backgroundCheck", fallbackMethod = "createCheckFallback")
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
        return handleFallback(e);
    }

    /** 배경 조회 단건 결과 */
    @CircuitBreaker(name = "backgroundCheck", fallbackMethod = "getCheckFallback")
    public BackgroundCheckResultResponse getCheck(String checkId) {
        return backgroundCheckClient.getCheck(checkId);
    }

    private BackgroundCheckResultResponse getCheckFallback(String checkId, Exception e) {
        return handleFallback(e);
    }

    /** 직원별 배경 조회 이력 */
    @CircuitBreaker(name = "backgroundCheck", fallbackMethod = "listChecksFallback")
    public BackgroundCheckListResponse listChecks(String employeeId) {
        return backgroundCheckClient.listChecks(employeeId);
    }

    private BackgroundCheckListResponse listChecksFallback(String employeeId, Exception e) {
        return handleFallback(e);
    }

    /**
     * 공통 폴백 처리
     * - BusinessException / ExternalApiException은 그대로 re-throw (비즈니스적 오류)
     * - 나머지(CallNotPermittedException, 연결 실패 등)는 EXTERNAL_API_UNAVAILABLE
     */
    private <T> T handleFallback(Exception e) {
        if (e instanceof BusinessException || e instanceof ExternalApiException) {
            throw (RuntimeException) e;
        }
        log.error("CircuitBreaker fallback triggered: {}", e.getMessage());
        throw new ExternalApiException(ErrorCode.EXTERNAL_API_UNAVAILABLE);
    }
}
