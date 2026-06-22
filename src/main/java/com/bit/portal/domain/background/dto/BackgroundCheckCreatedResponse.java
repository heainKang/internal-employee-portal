package com.bit.portal.domain.background.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class BackgroundCheckCreatedResponse {

    private String checkId;
    private String employeeId;
    private String status;
    private OffsetDateTime createdAt;
    private String message;
}
