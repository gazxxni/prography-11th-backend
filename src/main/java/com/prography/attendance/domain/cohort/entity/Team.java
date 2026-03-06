package com.prography.attendance.domain.cohort.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Column(nullable = false)
    private String name;

    protected Team() {
    }

    public Team(Cohort cohort, String name) {
        this.cohort = cohort;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Cohort getCohort() {
        return cohort;
    }

    public String getName() {
        return name;
    }
}
