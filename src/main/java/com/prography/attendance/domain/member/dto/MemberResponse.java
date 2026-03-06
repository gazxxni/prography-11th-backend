package com.prography.attendance.domain.member.dto;

import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import java.time.Instant;

public record MemberResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
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
