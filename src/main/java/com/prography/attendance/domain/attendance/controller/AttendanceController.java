package com.prography.attendance.domain.attendance.controller;

import com.prography.attendance.domain.attendance.dto.AdminAttendanceCreateRequest;
import com.prography.attendance.domain.attendance.dto.AdminAttendanceUpdateRequest;
import com.prography.attendance.domain.attendance.dto.AttendanceCheckRequest;
import com.prography.attendance.domain.attendance.dto.AttendanceResponse;
import com.prography.attendance.domain.attendance.dto.AttendanceSummaryResponse;
import com.prography.attendance.domain.attendance.dto.MemberAttendanceDetailResponse;
import com.prography.attendance.domain.attendance.dto.MyAttendanceResponse;
import com.prography.attendance.domain.attendance.dto.SessionAttendanceDetailResponse;
import com.prography.attendance.domain.attendance.dto.SessionAttendanceSummaryItem;
import com.prography.attendance.domain.attendance.service.AttendanceService;
import com.prography.attendance.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/attendances")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkAttendance(@Valid @RequestBody AttendanceCheckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attendanceService.checkAttendance(request)));
    }

    @GetMapping("/attendances")
    public ApiResponse<List<MyAttendanceResponse>> getMyAttendances(@RequestParam Long memberId) {
        return ApiResponse.success(attendanceService.getMyAttendances(memberId));
    }

    @GetMapping("/members/{memberId}/attendance-summary")
    public ApiResponse<AttendanceSummaryResponse> getAttendanceSummary(@PathVariable Long memberId) {
        return ApiResponse.success(attendanceService.getAttendanceSummary(memberId));
    }

    @PostMapping("/admin/attendances")
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(@Valid @RequestBody AdminAttendanceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attendanceService.createAttendance(request)));
    }

    @PutMapping("/admin/attendances/{id}")
    public ApiResponse<AttendanceResponse> updateAttendance(@PathVariable Long id, @Valid @RequestBody AdminAttendanceUpdateRequest request) {
        return ApiResponse.success(attendanceService.updateAttendance(id, request));
    }

    @GetMapping("/admin/attendances/sessions/{sessionId}/summary")
    public ApiResponse<List<SessionAttendanceSummaryItem>> getSessionSummary(@PathVariable Long sessionId) {
        return ApiResponse.success(attendanceService.getSessionSummary(sessionId));
    }

    @GetMapping("/admin/attendances/members/{memberId}")
    public ApiResponse<MemberAttendanceDetailResponse> getMemberAttendanceDetail(@PathVariable Long memberId) {
        return ApiResponse.success(attendanceService.getMemberAttendanceDetail(memberId));
    }

    @GetMapping("/admin/attendances/sessions/{sessionId}")
    public ApiResponse<SessionAttendanceDetailResponse> getSessionAttendances(@PathVariable Long sessionId) {
        return ApiResponse.success(attendanceService.getSessionAttendances(sessionId));
    }
}
