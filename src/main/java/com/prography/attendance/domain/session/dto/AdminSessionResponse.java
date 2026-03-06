package com.prography.attendance.domain.session.dto;

import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AdminSessionResponse(
        Long id,
        Long cohortId,
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status,
        AttendanceSummary attendanceSummary,
        boolean qrActive,
        Instant createdAt,
        Instant updatedAt
) {
    public static AdminSessionResponse of(Session session, AttendanceSummary attendanceSummary, boolean qrActive) {
        return new AdminSessionResponse(
                session.getId(),
                session.getCohort().getId(),
                session.getTitle(),
                session.getDate(),
                session.getTime(),
                session.getLocation(),
                session.getStatus(),
                attendanceSummary,
                qrActive,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    public record AttendanceSummary(int present, int absent, int late, int excused, int total) {
    }
}
