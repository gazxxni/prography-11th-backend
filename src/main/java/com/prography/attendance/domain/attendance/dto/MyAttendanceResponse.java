package com.prography.attendance.domain.attendance.dto;

import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import java.time.Instant;

public record MyAttendanceResponse(
        Long id,
        Long sessionId,
        String sessionTitle,
        AttendanceStatus status,
        Integer lateMinutes,
        Integer penaltyAmount,
        String reason,
        Instant checkedInAt,
        Instant createdAt
) {
    public static MyAttendanceResponse from(Attendance attendance) {
        return new MyAttendanceResponse(
                attendance.getId(),
                attendance.getSession().getId(),
                attendance.getSession().getTitle(),
                attendance.getStatus(),
                attendance.getLateMinutes(),
                attendance.getPenaltyAmount(),
                attendance.getReason(),
                attendance.getCheckedInAt(),
                attendance.getCreatedAt()
        );
    }
}
