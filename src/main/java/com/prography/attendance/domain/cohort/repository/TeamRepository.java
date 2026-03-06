package com.prography.attendance.domain.cohort.repository;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByCohortOrderByIdAsc(Cohort cohort);
    Optional<Team> findByIdAndCohort(Long id, Cohort cohort);
}
