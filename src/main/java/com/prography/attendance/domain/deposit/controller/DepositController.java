package com.prography.attendance.domain.deposit.controller;

import com.prography.attendance.domain.deposit.dto.DepositHistoryResponse;
import com.prography.attendance.domain.deposit.service.DepositQueryService;
import com.prography.attendance.global.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/cohort-members")
public class DepositController {

    private final DepositQueryService depositQueryService;

    public DepositController(DepositQueryService depositQueryService) {
        this.depositQueryService = depositQueryService;
    }

    @GetMapping("/{cohortMemberId}/deposits")
    public ApiResponse<List<DepositHistoryResponse>> getDepositHistories(@PathVariable Long cohortMemberId) {
        return ApiResponse.success(depositQueryService.getDepositHistories(cohortMemberId));
    }
}
