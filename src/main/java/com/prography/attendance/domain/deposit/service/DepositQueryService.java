package com.prography.attendance.domain.deposit.service;

import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.cohort.repository.CohortMemberRepository;
import com.prography.attendance.domain.deposit.dto.DepositHistoryResponse;
import com.prography.attendance.domain.deposit.repository.DepositHistoryRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepositQueryService {

    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    public DepositQueryService(CohortMemberRepository cohortMemberRepository, DepositHistoryRepository depositHistoryRepository) {
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositHistoryRepository = depositHistoryRepository;
    }

    public List<DepositHistoryResponse> getDepositHistories(Long cohortMemberId) {
        CohortMember cohortMember = cohortMemberRepository.findById(cohortMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return depositHistoryRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember).stream()
                .map(DepositHistoryResponse::from)
                .toList();
    }
}
