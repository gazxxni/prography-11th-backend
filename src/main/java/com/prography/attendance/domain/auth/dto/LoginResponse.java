package com.prography.attendance.domain.auth.dto;

import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import java.time.Instant;

public record LoginResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Instant createdAt,
        Instant updatedAt
) {
    public static LoginResponse from(Member member) {
        return new LoginResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
