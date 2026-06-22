package com.bit.portal.domain.background.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/** 외부 Background Check API 에 전송하는 실제 페이로드 */
@Getter
@Builder
public class BackgroundCheckApiRequest {

    private String employeeId;
    private String firstName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dateOfBirth;
}
