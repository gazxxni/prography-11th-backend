package com.prography.attendance.domain.deposit.entity;

import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.cohort.entity.CohortMember;
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
import java.time.Instant;

@Entity
@Table(name = "deposit_histories")
public class DepositHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_member_id", nullable = false)
    private CohortMember cohortMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id")
    private Attendance attendance;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected DepositHistory() {
    }

    public DepositHistory(CohortMember cohortMember, DepositType type, Integer amount, Integer balanceAfter, Attendance attendance, String description, Instant createdAt) {
        this.cohortMember = cohortMember;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.attendance = attendance;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public CohortMember getCohortMember() {
        return cohortMember;
    }

    public DepositType getType() {
        return type;
    }

    public Integer getAmount() {
        return amount;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
