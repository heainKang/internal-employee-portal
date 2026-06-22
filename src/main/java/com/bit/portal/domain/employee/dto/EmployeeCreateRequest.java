package com.bit.portal.domain.employee.dto;

import com.bit.portal.domain.employee.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EmployeeCreateRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름(First Name)은 필수입니다.")
    private String firstName;

    @NotBlank(message = "성(Last Name)은 필수입니다.")
    private String lastName;

    @NotNull(message = "생년월일은 필수입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate dateOfBirth;

    private String department;

    private String position;

    @NotNull(message = "입사일은 필수입니다.")
    private LocalDate hireDate;

    @NotNull(message = "권한은 필수입니다.")
    private Role role;
}
