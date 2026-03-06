package com.prography.attendance.domain.deposit.service;

import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.deposit.entity.DepositHistory;
import com.prography.attendance.domain.deposit.entity.DepositType;
import com.prography.attendance.domain.deposit.repository.DepositHistoryRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DepositService {

    private static final int INITIAL_DEPOSIT = 100000;

    private final DepositHistoryRepository depositHistoryRepository;
    private final Clock clock;

    public DepositService(DepositHistoryRepository depositHistoryRepository, Clock clock) {
        this.depositHistoryRepository = depositHistoryRepository;
        this.clock = clock;
    }

    public void applyInitialDeposit(CohortMember cohortMember) {
        cohortMember.setDeposit(INITIAL_DEPOSIT);
        depositHistoryRepository.save(new DepositHistory(
                cohortMember,
                DepositType.INITIAL,
                INITIAL_DEPOSIT,
                INITIAL_DEPOSIT,
                null,
                "초기 보증금",
                Instant.now(clock)
        ));
    }

    public void deductPenalty(CohortMember cohortMember, int amount, Attendance attendance) {
        if (amount <= 0) {
            return;
        }
        int current = cohortMember.getDeposit();
        if (current < amount) {
            throw new BusinessException(ErrorCode.DEPOSIT_INSUFFICIENT);
        }
        int balanceAfter = current - amount;
        cohortMember.setDeposit(balanceAfter);
        depositHistoryRepository.save(new DepositHistory(
                cohortMember,
                DepositType.PENALTY,
                -amount,
                balanceAfter,
                attendance,
                "출결 패널티 차감",
                Instant.now(clock)
        ));
    }

    public void refund(CohortMember cohortMember, int amount, Attendance attendance) {
        if (amount <= 0) {
            return;
        }
        int balanceAfter = cohortMember.getDeposit() + amount;
        cohortMember.setDeposit(balanceAfter);
        depositHistoryRepository.save(new DepositHistory(
                cohortMember,
                DepositType.REFUND,
                amount,
                balanceAfter,
                attendance,
                "출결 패널티 환급",
                Instant.now(clock)
        ));
    }
}
