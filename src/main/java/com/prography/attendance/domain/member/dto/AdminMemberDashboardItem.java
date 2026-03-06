package com.prography.attendance.domain.member.dto;

import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import java.time.Instant;

public record AdminMemberDashboardItem(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Integer generation,
        String partName,
        String teamName,
        Integer deposit,
        Instant createdAt,
        Instant updatedAt
) {
}
