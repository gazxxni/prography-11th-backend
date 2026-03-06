package com.prography.attendance.domain.session.dto;

import com.prography.attendance.domain.session.entity.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateSessionRequest(
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status
) {
}
