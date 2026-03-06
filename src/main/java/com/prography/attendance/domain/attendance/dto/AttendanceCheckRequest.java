package com.prography.attendance.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttendanceCheckRequest(
        @NotBlank String hashValue,
        @NotNull Long memberId
) {
}
