package com.prography.attendance.domain.cohort.dto;

import java.time.Instant;
import java.util.List;

public record CohortDetailResponse(
        Long id,
        Integer generation,
        String name,
        List<Item> parts,
        List<Item> teams,
        Instant createdAt
) {
    public record Item(Long id, String name) {
    }
}
