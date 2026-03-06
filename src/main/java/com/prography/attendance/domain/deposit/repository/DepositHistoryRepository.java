package com.prography.attendance.domain.deposit.repository;

import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.deposit.entity.DepositHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {
    List<DepositHistory> findByCohortMemberOrderByCreatedAtDesc(CohortMember cohortMember);
}
