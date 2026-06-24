package com.bit.portal.domain.employee.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeUpdateRequest {

    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;
}
