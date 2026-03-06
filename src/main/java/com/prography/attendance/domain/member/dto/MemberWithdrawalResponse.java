package com.prography.attendance.domain.member.dto;

import com.prography.attendance.domain.member.entity.MemberStatus;
import java.time.Instant;

public record MemberWithdrawalResponse(
        Long id,
        String loginId,
        String name,
        MemberStatus status,
        Instant updatedAt
) {
}
