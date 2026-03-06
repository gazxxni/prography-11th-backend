package com.prography.attendance.domain.cohort.repository;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortMemberRepository extends JpaRepository<CohortMember, Long> {
    Optional<CohortMember> findByCohortAndMember(Cohort cohort, Member member);
    Optional<CohortMember> findByCohortGenerationAndMemberId(Integer generation, Long memberId);
    Optional<CohortMember> findFirstByMemberIdOrderByCohortGenerationDesc(Long memberId);
    List<CohortMember> findByCohortOrderByIdAsc(Cohort cohort);
}
