package com.bit.portal.domain.employee.dto;

import com.bit.portal.domain.employee.enums.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeAdminUpdateRequest {

    @Size(max = 100, message = "부서명은 100자 이내여야 합니다.")
    private String department;

    @Size(max = 100, message = "직책은 100자 이내여야 합니다.")
    private String position;

    @NotNull(message = "권한은 필수입니다.")
    private Role role;
}
