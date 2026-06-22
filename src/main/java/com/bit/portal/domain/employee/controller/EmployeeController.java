package com.bit.portal.domain.employee.controller;

import com.bit.portal.domain.employee.dto.EmployeeResponse;
import com.bit.portal.domain.employee.dto.EmployeeUpdateRequest;
import com.bit.portal.domain.employee.service.EmployeeService;
import com.bit.portal.global.response.ApiResponse;
import com.bit.portal.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "직원 본인 정보", description = "로그인한 직원의 개인 정보 조회 및 수정")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "내 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        EmployeeResponse response = employeeService.getMyProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "내 정보 수정", description = "이메일, 전화번호 수정 가능")
    @PatchMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateMyProfile(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.of(response, "개인 정보가 수정되었습니다."));
    }
}
