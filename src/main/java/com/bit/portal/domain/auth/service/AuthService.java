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
    private final LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (loginAttemptService.isLocked(email)) {
            throw new BusinessException(ErrorCode.LOGIN_RATE_LIMIT);
        }

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(email);
                    return new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            loginAttemptService.loginFailed(email);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (employee.getStatus() == EmployeeStatus.RESIGNED) {
            loginAttemptService.loginFailed(email);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        loginAttemptService.loginSucceeded(email);
        String token = jwtTokenProvider.generateToken(employee);
        return LoginResponse.of(token, employee);
    }
}
