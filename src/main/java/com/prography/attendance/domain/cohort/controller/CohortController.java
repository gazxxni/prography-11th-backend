package com.prography.attendance.domain.cohort.controller;

import com.prography.attendance.domain.cohort.dto.CohortDetailResponse;
import com.prography.attendance.domain.cohort.dto.CohortSummaryResponse;
import com.prography.attendance.domain.cohort.service.CohortService;
import com.prography.attendance.global.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cohorts")
public class CohortController {

    private final CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }

    @GetMapping
    public ApiResponse<List<CohortSummaryResponse>> getCohorts() {
        return ApiResponse.success(cohortService.getCohorts());
    }

    @GetMapping("/{cohortId}")
    public ApiResponse<CohortDetailResponse> getCohort(@PathVariable Long cohortId) {
        return ApiResponse.success(cohortService.getCohort(cohortId));
    }
}
