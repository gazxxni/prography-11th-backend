package com.prography.attendance.domain.attendance.service;

import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

@Component
public class PenaltyCalculator {

    public int calculate(AttendanceStatus status, Integer lateMinutes) {
        return switch (status) {
            case PRESENT, EXCUSED -> 0;
            case ABSENT -> 10000;
            case LATE -> {
                if (lateMinutes == null || lateMinutes < 0) {
                    throw new IllegalArgumentException("lateMinutes must be provided for LATE");
                }
                yield Math.min(lateMinutes * 500, 10000);
            }
        };
    }
}
