package com.prography.attendance.domain.session.service;

import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import com.prography.attendance.domain.attendance.repository.AttendanceRepository;
import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.service.CurrentCohortService;
import com.prography.attendance.domain.session.dto.AdminSessionResponse;
import com.prography.attendance.domain.session.dto.CreateSessionRequest;
import com.prography.attendance.domain.session.dto.SessionSummaryResponse;
import com.prography.attendance.domain.session.dto.UpdateSessionRequest;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import com.prography.attendance.domain.session.repository.SessionRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final CurrentCohortService currentCohortService;
    private final QrCodeService qrCodeService;

    public SessionService(
            SessionRepository sessionRepository,
            AttendanceRepository attendanceRepository,
            CurrentCohortService currentCohortService,
            QrCodeService qrCodeService
    ) {
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.currentCohortService = currentCohortService;
        this.qrCodeService = qrCodeService;
    }

    public List<SessionSummaryResponse> getSessionsForMember() {
        Cohort current = currentCohortService.getCurrentCohort();
        return sessionRepository.findByCohortAndStatusNotOrderByDateAscTimeAsc(current, SessionStatus.CANCELLED).stream()
                .map(SessionSummaryResponse::from)
                .toList();
    }

    public List<AdminSessionResponse> getSessionsForAdmin(java.time.LocalDate dateFrom, java.time.LocalDate dateTo, SessionStatus status) {
        Cohort current = currentCohortService.getCurrentCohort();
        return sessionRepository.findByCohortOrderByDateAscTimeAsc(current).stream()
                .filter(session -> dateFrom == null || !session.getDate().isBefore(dateFrom))
                .filter(session -> dateTo == null || !session.getDate().isAfter(dateTo))
                .filter(session -> status == null || session.getStatus() == status)
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public AdminSessionResponse createSession(CreateSessionRequest request) {
        Session session = sessionRepository.save(new Session(
                currentCohortService.getCurrentCohort(),
                request.title(),
                request.date(),
                request.time(),
                request.location(),
                SessionStatus.SCHEDULED
        ));
        qrCodeService.create(session);
        return toAdminResponse(session);
    }

    @Transactional
    public AdminSessionResponse updateSession(Long id, UpdateSessionRequest request) {
        Session session = getSessionEntity(id);
        validateNotCancelled(session);
        session.update(request.title(), request.date(), request.time(), request.location(), request.status());
        return toAdminResponse(session);
    }

    @Transactional
    public AdminSessionResponse deleteSession(Long id) {
        Session session = getSessionEntity(id);
        validateNotCancelled(session);
        session.cancel();
        return toAdminResponse(session);
    }

    public Session getSessionEntity(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    private void validateNotCancelled(Session session) {
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_CANCELLED);
        }
    }

    private AdminSessionResponse toAdminResponse(Session session) {
        List<com.prography.attendance.domain.attendance.entity.Attendance> attendances = attendanceRepository.findBySessionOrderByIdAsc(session);
        int present = (int) attendances.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.PRESENT).count();
        int absent = (int) attendances.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.ABSENT).count();
        int late = (int) attendances.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.LATE).count();
        int excused = (int) attendances.stream().filter(attendance -> attendance.getStatus() == AttendanceStatus.EXCUSED).count();
        return AdminSessionResponse.of(
                session,
                new AdminSessionResponse.AttendanceSummary(present, absent, late, excused, attendances.size()),
                qrCodeService.hasActiveQr(session)
        );
    }
}
