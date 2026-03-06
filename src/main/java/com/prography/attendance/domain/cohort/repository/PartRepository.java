package com.prography.attendance.domain.cohort.repository;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.Part;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByCohortOrderByIdAsc(Cohort cohort);
    Optional<Part> findByIdAndCohort(Long id, Cohort cohort);
}
