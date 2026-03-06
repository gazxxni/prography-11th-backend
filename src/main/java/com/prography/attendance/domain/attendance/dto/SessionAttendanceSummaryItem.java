package com.prography.attendance.domain.attendance.dto;

public record SessionAttendanceSummaryItem(
        Long memberId,
        String memberName,
        int present,
        int absent,
        int late,
        int excused,
        int totalPenalty,
        Integer deposit
) {
}
