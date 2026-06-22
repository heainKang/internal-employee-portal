package com.bit.portal.domain.employee.dto;

import com.bit.portal.domain.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmployeeStatusUpdateRequest {

    @NotNull(message = "상태값은 필수입니다.")
    private EmployeeStatus status;
}
