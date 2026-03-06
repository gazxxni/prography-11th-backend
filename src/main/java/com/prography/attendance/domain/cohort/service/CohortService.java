package com.prography.attendance.domain.cohort.service;

import com.prography.attendance.domain.cohort.dto.CohortDetailResponse;
import com.prography.attendance.domain.cohort.dto.CohortSummaryResponse;
import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.repository.CohortRepository;
import com.prography.attendance.domain.cohort.repository.PartRepository;
import com.prography.attendance.domain.cohort.repository.TeamRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CohortService {

    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;

    public CohortService(CohortRepository cohortRepository, PartRepository partRepository, TeamRepository teamRepository) {
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
    }

    public List<CohortSummaryResponse> getCohorts() {
        return cohortRepository.findAll().stream()
                .map(CohortSummaryResponse::from)
                .toList();
    }

    public CohortDetailResponse getCohort(Long cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
        List<CohortDetailResponse.Item> parts = partRepository.findByCohortOrderByIdAsc(cohort).stream()
                .map(part -> new CohortDetailResponse.Item(part.getId(), part.getName()))
                .toList();
        List<CohortDetailResponse.Item> teams = teamRepository.findByCohortOrderByIdAsc(cohort).stream()
                .map(team -> new CohortDetailResponse.Item(team.getId(), team.getName()))
                .toList();
        return new CohortDetailResponse(cohort.getId(), cohort.getGeneration(), cohort.getName(), parts, teams, cohort.getCreatedAt());
    }
}
