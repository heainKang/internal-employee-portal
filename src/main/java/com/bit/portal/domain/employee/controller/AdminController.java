package com.bit.portal.domain.employee.controller;

import com.bit.portal.domain.employee.dto.*;
import com.bit.portal.domain.employee.service.EmployeeService;
import com.bit.portal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 — 직원 관리", description = "직원 계정 생성, 목록 조회, 상태 변경 (ROLE_ADMIN 전용)")
@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeService employeeService;

    @Operation(summary = "직원 계정 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(response, "직원 계정이 생성되었습니다."));
    }

    @Operation(summary = "전체 직원 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> response = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "직원 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployee(id);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "직원 상태 변경 (퇴사 처리 포함)",
            description = "status: RESIGNED 설정 시 즉시 계정 차단 및 기존 토큰 무효화")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request) {
        EmployeeResponse response = employeeService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.of(response, "직원 상태가 변경되었습니다."));
    }
}
