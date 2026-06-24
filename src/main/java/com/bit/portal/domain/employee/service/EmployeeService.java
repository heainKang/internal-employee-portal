package com.bit.portal.domain.employee.service;

import com.bit.portal.domain.employee.dto.*;
import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.EmployeeStatus;
import com.bit.portal.domain.employee.repository.EmployeeRepository;
import com.bit.portal.global.error.code.ErrorCode;
import com.bit.portal.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Admin: 직원 생성 ──────────────────────────────────────────────────────

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String employeeId = generateEmployeeId();
        Employee employee = Employee.create(
                employeeId,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                request.getDepartment(),
                request.getPosition(),
                request.getDateOfBirth(),
                request.getHireDate(),
                request.getRole()
        );

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    // ── Admin: 직원 목록 (페이지네이션 + 정렬) ────────────────────────────────

    public Page<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return employeeRepository.findAll(pageable).map(EmployeeResponse::from);
    }

    // ── Admin: 직원 상세 ──────────────────────────────────────────────────────

    public EmployeeResponse getEmployee(Long id) {
        return EmployeeResponse.from(findById(id));
    }

    // ── Admin: 상태 변경 (퇴사 처리만 허용) ──────────────────────────────────

    @Transactional
    public EmployeeResponse updateStatus(Long id, EmployeeStatusUpdateRequest request) {
        Employee employee = findById(id);

        if (employee.getStatus() == EmployeeStatus.RESIGNED) {
            throw new BusinessException(ErrorCode.ALREADY_RESIGNED);
        }

        if (request.getStatus() == EmployeeStatus.RESIGNED) {
            employee.resign();
        }

        return EmployeeResponse.from(employee);
    }

    // ── 직원 본인: 내 정보 조회/수정 ──────────────────────────────────────────

    public EmployeeResponse getMyProfile(String email) {
        return EmployeeResponse.from(findByEmail(email));
    }

    @Transactional
    public EmployeeResponse updateMyProfile(String email, EmployeeUpdateRequest request) {
        Employee employee = findByEmail(email);
        employee.updateProfile(request.getPhone());
        return EmployeeResponse.from(employee);
    }

    // ── 초기 Admin 계정 생성 (DataInitializer 전용) ───────────────────────────

    @Transactional
    public void createInitialAdmin(String email, String rawPassword) {
        if (employeeRepository.existsByEmail(email)) {
            return;
        }
        Employee admin = Employee.create(
                "EMP-" + LocalDate.now().getYear() + "-001",
                email,
                passwordEncoder.encode(rawPassword),
                "Admin", "System",
                "IT", "System Administrator",
                LocalDate.of(1990, 1, 1),
                LocalDate.now(),
                com.bit.portal.domain.employee.enums.Role.ROLE_ADMIN
        );
        employeeRepository.save(admin);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    private String generateEmployeeId() {
        int year = LocalDate.now().getYear();
        String prefix = "EMP-" + year + "-";
        // MAX 방식: 삭제 후 재생성 시 중복 없음, count 방식 대비 안전
        int next = employeeRepository.findMaxEmployeeIdByPrefix(prefix)
                .map(maxId -> Integer.parseInt(maxId.substring(prefix.length())) + 1)
                .orElse(1);
        return String.format("EMP-%d-%03d", year, next);
    }
}
