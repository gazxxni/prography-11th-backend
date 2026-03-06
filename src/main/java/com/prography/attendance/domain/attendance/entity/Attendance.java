package com.prography.attendance.domain.attendance.entity;

import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "attendances", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "cohort_member_id"}))
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_member_id", nullable = false)
    private CohortMember cohortMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private Integer lateMinutes;

    @Column(nullable = false)
    private Integer penaltyAmount;

    private String reason;

    private Instant checkedInAt;

    protected Attendance() {
    }

    public Attendance(Session session, CohortMember cohortMember, AttendanceStatus status, Integer lateMinutes, Integer penaltyAmount, String reason, Instant checkedInAt) {
        this.session = session;
        this.cohortMember = cohortMember;
        this.status = status;
        this.lateMinutes = lateMinutes;
        this.penaltyAmount = penaltyAmount;
        this.reason = reason;
        this.checkedInAt = checkedInAt;
    }

    public Long getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public CohortMember getCohortMember() {
        return cohortMember;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public Integer getPenaltyAmount() {
        return penaltyAmount;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void update(AttendanceStatus status, Integer lateMinutes, Integer penaltyAmount, String reason, Instant checkedInAt) {
        this.status = status;
        this.lateMinutes = lateMinutes;
        this.penaltyAmount = penaltyAmount;
        this.reason = reason;
        this.checkedInAt = checkedInAt;
    }
}
