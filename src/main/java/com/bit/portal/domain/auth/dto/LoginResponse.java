package com.bit.portal.domain.auth.dto;

import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private Long employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public static LoginResponse of(String accessToken, Employee employee) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .employeeId(employee.getId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .role(employee.getRole())
                .build();
    }
}
