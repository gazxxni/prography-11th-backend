package com.prography.attendance.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminMemberCreateRequest(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String name,
        @NotBlank String phone,
        @NotNull Long cohortId,
        Long partId,
        Long teamId
) {
}
