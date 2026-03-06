package com.prography.attendance.domain.cohort.entity;

import com.prography.attendance.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cohort_members", uniqueConstraints = @UniqueConstraint(columnNames = {"cohort_id", "member_id"}))
public class CohortMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer deposit;

    private Integer excuseCount;

    protected CohortMember() {
    }

    public CohortMember(Cohort cohort, Member member, Part part, Team team, Integer deposit, Integer excuseCount) {
        this.cohort = cohort;
        this.member = member;
        this.part = part;
        this.team = team;
        this.deposit = deposit;
        this.excuseCount = excuseCount;
    }

    public Long getId() {
        return id;
    }

    public Cohort getCohort() {
        return cohort;
    }

    public Member getMember() {
        return member;
    }

    public Part getPart() {
        return part;
    }

    public Team getTeam() {
        return team;
    }

    public Integer getDeposit() {
        return deposit;
    }

    public Integer getExcuseCount() {
        return excuseCount;
    }

    public void updateAssignment(Part part, Team team) {
        this.part = part;
        this.team = team;
    }

    public void setDeposit(Integer deposit) {
        this.deposit = deposit;
    }

    public void increaseExcuseCount() {
        this.excuseCount = this.excuseCount + 1;
    }

    public void decreaseExcuseCount() {
        this.excuseCount = Math.max(0, this.excuseCount - 1);
    }
}
