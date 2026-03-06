package com.prography.attendance.domain.attendance.dto;

import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import java.time.Instant;

public record AttendanceResponse(
        Long id,
        Long sessionId,
        Long memberId,
        AttendanceStatus status,
        Integer lateMinutes,
        Integer penaltyAmount,
        String reason,
        Instant checkedInAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getSession().getId(),
                attendance.getCohortMember().getMember().getId(),
                attendance.getStatus(),
                attendance.getLateMinutes(),
                attendance.getPenaltyAmount(),
                attendance.getReason(),
                attendance.getCheckedInAt(),
                attendance.getCreatedAt(),
                attendance.getUpdatedAt()
        );
    }
}
