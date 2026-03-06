package com.prography.attendance.domain.session.service;

import com.prography.attendance.domain.session.dto.QrCodeResponse;
import com.prography.attendance.domain.session.entity.QrCode;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.repository.QrCodeRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final Clock clock;

    public QrCodeService(QrCodeRepository qrCodeRepository, Clock clock) {
        this.qrCodeRepository = qrCodeRepository;
        this.clock = clock;
    }

    @Transactional
    public QrCode create(Session session) {
        Instant now = Instant.now(clock);
        qrCodeRepository.findFirstBySessionAndExpiresAtAfterOrderByCreatedAtDesc(session, now)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.QR_ALREADY_ACTIVE);
                });
        return qrCodeRepository.save(new QrCode(session, UUID.randomUUID().toString(), now, now.plus(24, ChronoUnit.HOURS)));
    }

    @Transactional
    public QrCodeResponse renew(Long qrCodeId) {
        Instant now = Instant.now(clock);
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_NOT_FOUND));
        qrCode.expireAt(now);
        QrCode newQrCode = qrCodeRepository.save(new QrCode(
                qrCode.getSession(),
                UUID.randomUUID().toString(),
                now,
                now.plus(24, ChronoUnit.HOURS)
        ));
        return QrCodeResponse.from(newQrCode);
    }

    public boolean hasActiveQr(Session session) {
        return qrCodeRepository.findFirstBySessionAndExpiresAtAfterOrderByCreatedAtDesc(session, Instant.now(clock)).isPresent();
    }

    public QrCodeResponse create(Long sessionId, SessionService sessionService) {
        return QrCodeResponse.from(create(sessionService.getSessionEntity(sessionId)));
    }
}
