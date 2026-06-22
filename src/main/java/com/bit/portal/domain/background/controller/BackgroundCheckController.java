package com.bit.portal.domain.background.controller;

import com.bit.portal.domain.background.dto.*;
import com.bit.portal.domain.background.service.BackgroundCheckService;
import com.bit.portal.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "관리자 — 배경 조회", description = "Background Check API 연동 (ROLE_ADMIN 전용)")
@RestController
@RequestMapping("/api/admin/background-checks")
@RequiredArgsConstructor
public class BackgroundCheckController {

    private final BackgroundCheckService backgroundCheckService;

    @Operation(summary = "배경 조회 요청",
            description = "직원 ID로 배경 조회를 요청합니다. status=pending이면 결과 조회 API를 폴링하세요.")
    @PostMapping
    public ResponseEntity<ApiResponse<BackgroundCheckCreatedResponse>> createCheck(
            @Valid @RequestBody BackgroundCheckCreateRequest request) {
        BackgroundCheckCreatedResponse response = backgroundCheckService.createCheck(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(response, "배경 조회 요청이 접수되었습니다."));
    }

    @Operation(summary = "배경 조회 결과 단건 조회",
            description = "checkId로 결과를 조회합니다. status=pending이면 아직 처리 중입니다.")
    @GetMapping("/{checkId}")
    public ResponseEntity<ApiResponse<BackgroundCheckResultResponse>> getCheck(
            @PathVariable String checkId) {
        BackgroundCheckResultResponse response = backgroundCheckService.getCheck(checkId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "직원별 배경 조회 이력",
            description = "employeeId에 해당하는 모든 배경 조회 이력을 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<BackgroundCheckListResponse>> listChecks(
            @RequestParam String employeeId) {
        BackgroundCheckListResponse response = backgroundCheckService.listChecks(employeeId);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
