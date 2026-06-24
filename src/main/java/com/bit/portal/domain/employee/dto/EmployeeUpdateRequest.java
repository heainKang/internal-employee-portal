package com.bit.portal.domain.employee.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeUpdateRequest {

    @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @Size(max = 255, message = "주소는 255자 이내여야 합니다.")
    private String address;

    @Size(max = 100, message = "비상연락처 이름은 100자 이내여야 합니다.")
    private String emergencyContactName;

    @Pattern(regexp = "^$|^[0-9+\\-\\s]{7,20}$", message = "비상연락처 전화번호 형식이 올바르지 않습니다.")
    private String emergencyContactPhone;

    @Size(max = 500, message = "메모는 500자 이내여야 합니다.")
    private String note;
}
