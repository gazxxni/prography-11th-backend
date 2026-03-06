package com.prography.attendance.domain.cohort.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "cohorts")
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer generation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Cohort() {
    }

    public Cohort(Integer generation, String name, Instant createdAt) {
        this.generation = generation;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Integer getGeneration() {
        return generation;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
