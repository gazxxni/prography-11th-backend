package com.prography.attendance.domain.session.controller;

import com.prography.attendance.domain.session.dto.AdminSessionResponse;
import com.prography.attendance.domain.session.dto.CreateSessionRequest;
import com.prography.attendance.domain.session.dto.QrCodeResponse;
import com.prography.attendance.domain.session.dto.SessionSummaryResponse;
import com.prography.attendance.domain.session.dto.UpdateSessionRequest;
import com.prography.attendance.domain.session.entity.SessionStatus;
import com.prography.attendance.domain.session.service.QrCodeService;
import com.prography.attendance.domain.session.service.SessionService;
import com.prography.attendance.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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
public class SessionController {

    private final SessionService sessionService;
    private final QrCodeService qrCodeService;

    public SessionController(SessionService sessionService, QrCodeService qrCodeService) {
        this.sessionService = sessionService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionSummaryResponse>> getSessionsForMember() {
        return ApiResponse.success(sessionService.getSessionsForMember());
    }

    @GetMapping("/admin/sessions")
    public ApiResponse<List<AdminSessionResponse>> getSessionsForAdmin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) SessionStatus status
    ) {
        return ApiResponse.success(sessionService.getSessionsForAdmin(dateFrom, dateTo, status));
    }

    @PostMapping("/admin/sessions")
    public ResponseEntity<ApiResponse<AdminSessionResponse>> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(sessionService.createSession(request)));
    }

    @PutMapping("/admin/sessions/{id}")
    public ApiResponse<AdminSessionResponse> updateSession(@PathVariable Long id, @RequestBody UpdateSessionRequest request) {
        return ApiResponse.success(sessionService.updateSession(id, request));
    }

    @DeleteMapping("/admin/sessions/{id}")
    public ApiResponse<AdminSessionResponse> deleteSession(@PathVariable Long id) {
        return ApiResponse.success(sessionService.deleteSession(id));
    }

    @PostMapping("/admin/sessions/{sessionId}/qrcodes")
    public ResponseEntity<ApiResponse<QrCodeResponse>> createQrCode(@PathVariable Long sessionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(qrCodeService.create(sessionId, sessionService)));
    }

    @PutMapping("/admin/qrcodes/{qrCodeId}")
    public ApiResponse<QrCodeResponse> renewQrCode(@PathVariable Long qrCodeId) {
        return ApiResponse.success(qrCodeService.renew(qrCodeId));
    }
}
