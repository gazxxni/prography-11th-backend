package com.prography.attendance.domain.session.repository;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByCohortAndStatusNotOrderByDateAscTimeAsc(Cohort cohort, SessionStatus status);
    List<Session> findByCohortOrderByDateAscTimeAsc(Cohort cohort);
    List<Session> findByCohortAndDateBetweenOrderByDateAscTimeAsc(Cohort cohort, LocalDate dateFrom, LocalDate dateTo);
}
