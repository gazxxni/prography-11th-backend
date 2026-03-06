package com.prography.attendance.domain.member.controller;

import com.prography.attendance.domain.member.dto.AdminMemberCreateRequest;
import com.prography.attendance.domain.member.dto.AdminMemberDashboardItem;
import com.prography.attendance.domain.member.dto.AdminMemberDetailResponse;
import com.prography.attendance.domain.member.dto.AdminMemberUpdateRequest;
import com.prography.attendance.domain.member.dto.MemberResponse;
import com.prography.attendance.domain.member.dto.MemberWithdrawalResponse;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.service.MemberService;
import com.prography.attendance.global.common.ApiResponse;
import com.prography.attendance.global.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members/{id}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
        return ApiResponse.success(memberService.getMember(id));
    }

    @PostMapping("/admin/members")
    public ResponseEntity<ApiResponse<AdminMemberDetailResponse>> createMember(@Valid @RequestBody AdminMemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(memberService.createMember(request)));
    }

    @GetMapping("/admin/members")
    public ApiResponse<PageResponse<AdminMemberDashboardItem>> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) Integer generation,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) MemberStatus status
    ) {
        return ApiResponse.success(memberService.getMembers(page, size, searchType, searchValue, generation, partName, teamName, status));
    }

    @GetMapping("/admin/members/{id}")
    public ApiResponse<AdminMemberDetailResponse> getAdminMember(@PathVariable Long id) {
        return ApiResponse.success(memberService.getAdminMember(id));
    }

    @PutMapping("/admin/members/{id}")
    public ApiResponse<AdminMemberDetailResponse> updateMember(@PathVariable Long id, @Valid @RequestBody AdminMemberUpdateRequest request) {
        return ApiResponse.success(memberService.updateMember(id, request));
    }

    @DeleteMapping("/admin/members/{id}")
    public ApiResponse<MemberWithdrawalResponse> deleteMember(@PathVariable Long id) {
        return ApiResponse.success(memberService.deleteMember(id));
    }
}
