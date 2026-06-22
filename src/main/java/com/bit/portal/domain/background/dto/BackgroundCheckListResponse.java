package com.bit.portal.domain.background.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class BackgroundCheckListResponse {

    private String employeeId;
    private List<CheckSummary> checks;
    private Integer totalCount;

    @Getter
    @NoArgsConstructor
    public static class CheckSummary {
        private String checkId;
        private String status;
        private OffsetDateTime createdAt;
        private OffsetDateTime completedAt;
    }
}
