package com.bit.portal.domain.background.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class BackgroundCheckResultResponse {

    private String checkId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String status;
    private Boolean criminalRecord;
    private Boolean educationVerified;
    private Boolean employmentVerified;
    private String creditScore;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}
