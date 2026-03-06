package com.prography.attendance.domain.member.dto;

public record AdminMemberUpdateRequest(
        String name,
        String phone,
        Long cohortId,
        Long partId,
        Long teamId
) {
}
