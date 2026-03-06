package com.prography.attendance.domain.attendance.dto;

import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AdminAttendanceCreateRequest(
        @NotNull Long sessionId,
        @NotNull Long memberId,
        @NotNull AttendanceStatus status,
        Integer lateMinutes,
        String reason
) {
}
