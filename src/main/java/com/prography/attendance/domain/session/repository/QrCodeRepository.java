package com.prography.attendance.domain.session.repository;

import com.prography.attendance.domain.session.entity.QrCode;
import com.prography.attendance.domain.session.entity.Session;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    Optional<QrCode> findByHashValue(String hashValue);
    Optional<QrCode> findFirstBySessionAndExpiresAtAfterOrderByCreatedAtDesc(Session session, Instant now);
    List<QrCode> findBySessionOrderByCreatedAtDesc(Session session);
}
