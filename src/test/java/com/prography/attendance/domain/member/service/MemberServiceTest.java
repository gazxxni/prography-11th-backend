package com.prography.attendance.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.cohort.entity.Part;
import com.prography.attendance.domain.cohort.entity.Team;
import com.prography.attendance.domain.cohort.repository.CohortMemberRepository;
import com.prography.attendance.domain.cohort.repository.CohortRepository;
import com.prography.attendance.domain.cohort.repository.PartRepository;
import com.prography.attendance.domain.cohort.repository.TeamRepository;
import com.prography.attendance.domain.deposit.service.DepositService;
import com.prography.attendance.domain.member.dto.AdminMemberCreateRequest;
import com.prography.attendance.domain.member.dto.AdminMemberUpdateRequest;
import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.repository.MemberRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CohortRepository cohortRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private CohortMemberRepository cohortMemberRepository;
    @Mock
    private DepositService depositService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("중복 loginId로 회원 등록 시 예외가 발생한다")
    void createMemberDuplicateLoginId() {
        when(memberRepository.existsByLoginId("user1")).thenReturn(true);

        assertThatThrownBy(() -> memberService.createMember(new AdminMemberCreateRequest("user1", "password123", "홍길동", "010-1234-5678", 2L, 6L, 1L)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("회원 조회")
    void getMember() {
        Member member = member(1L, "user1", MemberStatus.ACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var response = memberService.getMember(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("회원 수정 시 기존 기수 소속이 없으면 새 CohortMember를 생성한다")
    void updateMemberCreatesCohortMember() {
        Member member = member(1L, "user1", MemberStatus.ACTIVE);
        Cohort cohort = cohort(2L, 11);
        Part part = part(6L, cohort, "SERVER");
        Team team = team(1L, cohort, "Team A");
        CohortMember created = cohortMember(10L, cohort, member, part, team, 0, 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(cohortMemberRepository.findFirstByMemberIdOrderByCohortGenerationDesc(1L)).thenReturn(Optional.empty());
        when(cohortRepository.findById(2L)).thenReturn(Optional.of(cohort));
        when(partRepository.findByIdAndCohort(6L, cohort)).thenReturn(Optional.of(part));
        when(teamRepository.findByIdAndCohort(1L, cohort)).thenReturn(Optional.of(team));
        when(cohortMemberRepository.findByCohortAndMember(cohort, member)).thenReturn(Optional.empty());
        when(cohortMemberRepository.save(any(CohortMember.class))).thenReturn(created);

        var response = memberService.updateMember(1L, new AdminMemberUpdateRequest("새이름", "010-1111-2222", 2L, 6L, 1L));

        assertThat(response.name()).isEqualTo("새이름");
        assertThat(response.generation()).isEqualTo(11);
        verify(depositService).applyInitialDeposit(created);
    }

    @Test
    @DisplayName("회원 탈퇴")
    void withdrawMember() {
        Member member = member(1L, "user1", MemberStatus.ACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var response = memberService.deleteMember(1L);

        assertThat(response.status()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(cohortMemberRepository, never()).save(any());
    }

    private Member member(Long id, String loginId, MemberStatus status) {
        Member member = new Member(loginId, passwordEncoder.encode("password123"), "홍길동", "010-1234-5678", MemberRole.MEMBER, status);
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(member, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        return member;
    }

    private Cohort cohort(Long id, int generation) {
        Cohort cohort = new Cohort(generation, generation + "기", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(cohort, "id", id);
        return cohort;
    }

    private Part part(Long id, Cohort cohort, String name) {
        Part part = new Part(cohort, name);
        ReflectionTestUtils.setField(part, "id", id);
        return part;
    }

    private Team team(Long id, Cohort cohort, String name) {
        Team team = new Team(cohort, name);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private CohortMember cohortMember(Long id, Cohort cohort, Member member, Part part, Team team, int deposit, int excuseCount) {
        CohortMember cohortMember = new CohortMember(cohort, member, part, team, deposit, excuseCount);
        ReflectionTestUtils.setField(cohortMember, "id", id);
        return cohortMember;
    }
}
