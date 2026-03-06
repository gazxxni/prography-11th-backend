package com.prography.attendance.domain.cohort.service;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.repository.CohortRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrentCohortService {

    private final CohortRepository cohortRepository;
    private final Integer currentCohortNumber;

    public CurrentCohortService(CohortRepository cohortRepository, @Value("${app.current-cohort-number}") Integer currentCohortNumber) {
        this.cohortRepository = cohortRepository;
        this.currentCohortNumber = currentCohortNumber;
    }

    public Cohort getCurrentCohort() {
        return cohortRepository.findByGeneration(currentCohortNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
    }

    public Integer getCurrentCohortNumber() {
        return currentCohortNumber;
    }
}
