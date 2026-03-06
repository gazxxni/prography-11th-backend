package com.prography.attendance.domain.session.dto;

import com.prography.attendance.domain.session.entity.QrCode;
import java.time.Instant;

public record QrCodeResponse(
        Long id,
        Long sessionId,
        String hashValue,
        Instant createdAt,
        Instant expiresAt
) {
    public static QrCodeResponse from(QrCode qrCode) {
        return new QrCodeResponse(
                qrCode.getId(),
                qrCode.getSession().getId(),
                qrCode.getHashValue(),
                qrCode.getCreatedAt(),
                qrCode.getExpiresAt()
        );
    }
}
