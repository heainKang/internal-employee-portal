package com.bit.portal.domain.employee.dto;

import com.bit.portal.domain.employee.entity.Employee;
import com.bit.portal.domain.employee.enums.EmployeeStatus;
import com.bit.portal.domain.employee.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EmployeeResponse {

    private Long id;
    private String employeeId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String position;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .phone(employee.getPhone())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .dateOfBirth(employee.getDateOfBirth())
                .hireDate(employee.getHireDate())
                .status(employee.getStatus())
                .role(employee.getRole())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
