package com.prography.attendance.domain.deposit.dto;

import com.prography.attendance.domain.deposit.entity.DepositHistory;
import com.prography.attendance.domain.deposit.entity.DepositType;
import java.time.Instant;

public record DepositHistoryResponse(
        Long id,
        Long cohortMemberId,
        DepositType type,
        Integer amount,
        Integer balanceAfter,
        Long attendanceId,
        String description,
        Instant createdAt
) {
    public static DepositHistoryResponse from(DepositHistory history) {
        return new DepositHistoryResponse(
                history.getId(),
                history.getCohortMember().getId(),
                history.getType(),
                history.getAmount(),
                history.getBalanceAfter(),
                history.getAttendance() != null ? history.getAttendance().getId() : null,
                history.getDescription(),
                history.getCreatedAt()
        );
    }
}
