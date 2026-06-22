package com.bit.portal.domain.background.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 관리자가 요청하는 내부 DTO — employeeId만 받아 나머지는 DB에서 조회 */
@Getter
@NoArgsConstructor
public class BackgroundCheckCreateRequest {

    @NotBlank(message = "직원 ID는 필수입니다.")
    private String employeeId;
}
