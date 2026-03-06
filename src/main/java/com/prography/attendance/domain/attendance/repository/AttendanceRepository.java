package com.prography.attendance.domain.attendance.repository;

import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.session.entity.Session;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsBySessionAndCohortMember(Session session, CohortMember cohortMember);
    boolean existsBySessionAndCohortMemberMemberId(Session session, Long memberId);
    Optional<Attendance> findBySessionAndCohortMember(Session session, CohortMember cohortMember);
    List<Attendance> findByCohortMemberOrderByCreatedAtDesc(CohortMember cohortMember);
    List<Attendance> findBySessionOrderByIdAsc(Session session);
}
