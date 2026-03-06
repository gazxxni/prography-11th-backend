package com.prography.attendance.domain.attendance.dto;

public record AttendanceSummaryResponse(
        Long memberId,
        int present,
        int absent,
        int late,
        int excused,
        int totalPenalty,
        Integer deposit
) {
}
