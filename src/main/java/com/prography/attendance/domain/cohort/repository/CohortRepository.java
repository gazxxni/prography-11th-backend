package com.prography.attendance.domain.cohort.repository;

import com.prography.attendance.domain.cohort.entity.Cohort;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortRepository extends JpaRepository<Cohort, Long> {
    Optional<Cohort> findByGeneration(Integer generation);
}
