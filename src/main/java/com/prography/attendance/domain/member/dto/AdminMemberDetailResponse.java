package com.prography.attendance.domain.member.dto;

import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import java.time.Instant;

public record AdminMemberDetailResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Integer generation,
        String partName,
        String teamName,
        Instant createdAt,
        Instant updatedAt
) {
}
