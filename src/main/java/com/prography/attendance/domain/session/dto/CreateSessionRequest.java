package com.prography.attendance.domain.session.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSessionRequest(
        @NotBlank String title,
        @NotNull @FutureOrPresent LocalDate date,
        @NotNull LocalTime time,
        @NotBlank String location
) {
}
