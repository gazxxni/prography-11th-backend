package com.prography.attendance.domain.cohort.dto;

import com.prography.attendance.domain.cohort.entity.Cohort;
import java.time.Instant;

public record CohortSummaryResponse(
        Long id,
        Integer generation,
        String name,
        Instant createdAt
) {
    public static CohortSummaryResponse from(Cohort cohort) {
        return new CohortSummaryResponse(cohort.getId(), cohort.getGeneration(), cohort.getName(), cohort.getCreatedAt());
    }
}
