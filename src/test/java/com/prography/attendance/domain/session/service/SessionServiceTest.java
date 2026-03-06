package com.prography.attendance.domain.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prography.attendance.domain.attendance.repository.AttendanceRepository;
import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.service.CurrentCohortService;
import com.prography.attendance.domain.session.dto.CreateSessionRequest;
import com.prography.attendance.domain.session.dto.UpdateSessionRequest;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import com.prography.attendance.domain.session.repository.SessionRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private CurrentCohortService currentCohortService;
    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private SessionService sessionService;

    @Test
    @DisplayName("일정 생성 시 QR이 자동 생성된다")
    void createSessionCreatesQrCode() {
        Cohort cohort = cohort(2L, 11);
        Session session = session(1L, cohort, SessionStatus.SCHEDULED);
        when(currentCohortService.getCurrentCohort()).thenReturn(cohort);
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(attendanceRepository.findBySessionOrderByIdAsc(session)).thenReturn(List.of());
        when(qrCodeService.hasActiveQr(session)).thenReturn(true);

        var response = sessionService.createSession(new CreateSessionRequest("정기 모임", LocalDate.of(2026, 3, 10), LocalTime.of(14, 0), "강남"));

        assertThat(response.id()).isEqualTo(1L);
        verify(qrCodeService).create(session);
    }

    @Test
    @DisplayName("취소된 일정은 수정할 수 없다")
    void updateCancelledSessionFails() {
        Cohort cohort = cohort(2L, 11);
        Session session = session(1L, cohort, SessionStatus.CANCELLED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.updateSession(1L, new UpdateSessionRequest("변경", null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.SESSION_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("일정 삭제 시 CANCELLED로 변경된다")
    void deleteSession() {
        Cohort cohort = cohort(2L, 11);
        Session session = session(1L, cohort, SessionStatus.SCHEDULED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionOrderByIdAsc(session)).thenReturn(List.of());
        when(qrCodeService.hasActiveQr(session)).thenReturn(false);

        var response = sessionService.deleteSession(1L);

        assertThat(response.status()).isEqualTo(SessionStatus.CANCELLED);
    }

    private Cohort cohort(Long id, int generation) {
        Cohort cohort = new Cohort(generation, generation + "기", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(cohort, "id", id);
        return cohort;
    }

    private Session session(Long id, Cohort cohort, SessionStatus status) {
        Session session = new Session(cohort, "정기 모임", LocalDate.of(2026, 3, 10), LocalTime.of(14, 0), "강남", status);
        ReflectionTestUtils.setField(session, "id", id);
        ReflectionTestUtils.setField(session, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(session, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        return session;
    }
}
