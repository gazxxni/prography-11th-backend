package com.prography.attendance.domain.session.dto;

import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record SessionSummaryResponse(
        Long id,
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SessionSummaryResponse from(Session session) {
        return new SessionSummaryResponse(
                session.getId(),
                session.getTitle(),
                session.getDate(),
                session.getTime(),
                session.getLocation(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
