package com.bit.portal.domain.auth.service;

import com.bit.portal.domain.auth.dto.LoginRequest;
import com.bit.portal.domain.auth.dto.LoginResponse;
import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.EmployeeStatus;
import com.bit.portal.domain.employee.repository.EmployeeRepository;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import com.bit.portal.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 퇴사자 로그인 차단 — 실패 원인 노출 없이 401 통일
        if (employee.getStatus() == EmployeeStatus.RESIGNED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(employee);
        return LoginResponse.of(token, employee);
    }
}
