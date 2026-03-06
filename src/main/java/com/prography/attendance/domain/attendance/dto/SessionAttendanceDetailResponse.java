package com.prography.attendance.domain.attendance.dto;

import java.util.List;

public record SessionAttendanceDetailResponse(
        Long sessionId,
        String sessionTitle,
        List<AttendanceResponse> attendances
) {
}
