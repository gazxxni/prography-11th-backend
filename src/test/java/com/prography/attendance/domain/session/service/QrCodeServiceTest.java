package com.prography.attendance.domain.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.session.entity.QrCode;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import com.prography.attendance.domain.session.repository.QrCodeRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private QrCodeRepository qrCodeRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T01:00:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("활성 QR이 이미 있으면 새 QR 생성이 거부된다")
    void createFailsWhenActiveQrExists() {
        Session session = session(1L);
        QrCode active = qrCode(1L, session, Instant.parse("2026-03-02T01:00:00Z"));
        when(qrCodeRepository.findFirstBySessionAndExpiresAtAfterOrderByCreatedAtDesc(session, Instant.now(clock))).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> qrCodeService.create(session))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.QR_ALREADY_ACTIVE);
    }

    @Test
    @DisplayName("QR 갱신 시 기존 QR은 만료되고 새 QR이 생성된다")
    void renewQrCode() {
        Session session = session(1L);
        QrCode existing = qrCode(1L, session, Instant.parse("2026-03-02T01:00:00Z"));
        when(qrCodeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(qrCodeRepository.save(any(QrCode.class))).thenAnswer(invocation -> {
            QrCode qrCode = invocation.getArgument(0);
            ReflectionTestUtils.setField(qrCode, "id", 2L);
            return qrCode;
        });

        var response = qrCodeService.renew(1L);

        assertThat(existing.getExpiresAt()).isEqualTo(Instant.now(clock));
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.sessionId()).isEqualTo(1L);
    }

    private Session session(Long id) {
        Cohort cohort = new Cohort(11, "11기", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(cohort, "id", 2L);
        Session session = new Session(cohort, "정기 모임", LocalDate.of(2026, 3, 1), LocalTime.of(14, 0), "강남", SessionStatus.SCHEDULED);
        ReflectionTestUtils.setField(session, "id", id);
        ReflectionTestUtils.setField(session, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(session, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        return session;
    }

    private QrCode qrCode(Long id, Session session, Instant expiresAt) {
        QrCode qrCode = new QrCode(session, "hash-value", Instant.parse("2026-03-01T01:00:00Z"), expiresAt);
        ReflectionTestUtils.setField(qrCode, "id", id);
        return qrCode;
    }
}
