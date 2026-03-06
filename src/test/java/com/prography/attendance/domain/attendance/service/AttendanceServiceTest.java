package com.prography.attendance.domain.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prography.attendance.domain.attendance.dto.AdminAttendanceCreateRequest;
import com.prography.attendance.domain.attendance.dto.AdminAttendanceUpdateRequest;
import com.prography.attendance.domain.attendance.dto.AttendanceCheckRequest;
import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import com.prography.attendance.domain.attendance.repository.AttendanceRepository;
import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.cohort.entity.Part;
import com.prography.attendance.domain.cohort.repository.CohortMemberRepository;
import com.prography.attendance.domain.cohort.service.CurrentCohortService;
import com.prography.attendance.domain.deposit.service.DepositService;
import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.repository.MemberRepository;
import com.prography.attendance.domain.session.entity.QrCode;
import com.prography.attendance.domain.session.entity.Session;
import com.prography.attendance.domain.session.entity.SessionStatus;
import com.prography.attendance.domain.session.repository.QrCodeRepository;
import com.prography.attendance.domain.session.repository.SessionRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private QrCodeRepository qrCodeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CohortMemberRepository cohortMemberRepository;
    @Mock
    private CurrentCohortService currentCohortService;
    @Mock
    private DepositService depositService;

    @Spy
    private PenaltyCalculator penaltyCalculator;
    @Spy
    private ExcusePolicy excusePolicy;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T04:50:00Z"), ZoneId.of("Asia/Seoul"));

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    @DisplayName("QR 출석 체크는 시작 시간 이전이면 PRESENT다")
    void checkAttendancePresentBeforeStart() {
        TestFixture fixture = fixture(SessionStatus.IN_PROGRESS, Instant.parse("2026-03-02T00:00:00Z"));
        stubCheckInFlow(fixture);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> savedAttendance(1L, invocation.getArgument(0)));

        var response = attendanceService.checkAttendance(new AttendanceCheckRequest("hash", fixture.member.getId()));

        assertThat(response.status()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(response.penaltyAmount()).isZero();
        verify(depositService, never()).deductPenalty(any(), any(Integer.class), any());
    }

    @Test
    @DisplayName("QR 출석 체크는 7분 지각이면 3500원을 차감한다")
    void checkAttendanceLateSevenMinutes() {
        AttendanceService service = serviceWithClock(Instant.parse("2026-03-01T05:07:00Z"));
        TestFixture fixture = fixture(SessionStatus.IN_PROGRESS, Instant.parse("2026-03-02T00:00:00Z"));
        stubCheckInFlow(service, fixture);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> savedAttendance(1L, invocation.getArgument(0)));

        var response = service.checkAttendance(new AttendanceCheckRequest("hash", fixture.member.getId()));

        assertThat(response.status()).isEqualTo(AttendanceStatus.LATE);
        assertThat(response.lateMinutes()).isEqualTo(7);
        assertThat(response.penaltyAmount()).isEqualTo(3500);
        verify(depositService).deductPenalty(eq(fixture.cohortMember), eq(3500), any(Attendance.class));
    }

    @Test
    @DisplayName("검증 순서상 QR이 없으면 QR_INVALID가 먼저 발생한다")
    void checkAttendanceValidationOrder() {
        when(qrCodeRepository.findByHashValue("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkAttendance(new AttendanceCheckRequest("invalid", 1L)))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.QR_INVALID);
    }

    @Test
    @DisplayName("보증금이 부족하면 출석 체크가 실패한다")
    void checkAttendanceDepositInsufficient() {
        AttendanceService service = serviceWithClock(Instant.parse("2026-03-01T05:07:00Z"));
        TestFixture fixture = fixture(SessionStatus.IN_PROGRESS, Instant.parse("2026-03-02T00:00:00Z"));
        stubCheckInFlow(service, fixture);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> savedAttendance(1L, invocation.getArgument(0)));
        doThrow(new BusinessException(ErrorCode.DEPOSIT_INSUFFICIENT)).when(depositService).deductPenalty(any(), any(Integer.class), any());

        assertThatThrownBy(() -> service.checkAttendance(new AttendanceCheckRequest("hash", fixture.member.getId())))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.DEPOSIT_INSUFFICIENT);
    }

    @Test
    @DisplayName("출결 수정 시 LATE 3000원에서 ABSENT 10000원으로 바꾸면 7000원을 추가 차감한다")
    void updateAttendanceChargeDiff() {
        Attendance attendance = existingAttendance(1L, AttendanceStatus.LATE, 6, 3000);
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));

        var response = attendanceService.updateAttendance(1L, new AdminAttendanceUpdateRequest(AttendanceStatus.ABSENT, null, "무단 결석"));

        assertThat(response.penaltyAmount()).isEqualTo(10000);
        verify(depositService).deductPenalty(attendance.getCohortMember(), 7000, attendance);
    }

    @Test
    @DisplayName("출결 수정 시 ABSENT 10000원에서 PRESENT 0원으로 바꾸면 10000원을 환급한다")
    void updateAttendanceRefundDiff() {
        Attendance attendance = existingAttendance(1L, AttendanceStatus.ABSENT, null, 10000);
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));

        var response = attendanceService.updateAttendance(1L, new AdminAttendanceUpdateRequest(AttendanceStatus.PRESENT, null, null));

        assertThat(response.penaltyAmount()).isZero();
        verify(depositService).refund(attendance.getCohortMember(), 10000, attendance);
    }

    @Test
    @DisplayName("공결은 4회째부터 불가능하다")
    void createAttendanceExcuseLimitExceeded() {
        TestFixture fixture = fixture(SessionStatus.SCHEDULED, Instant.parse("2026-03-02T00:00:00Z"));
        ReflectionTestUtils.setField(fixture.cohortMember, "excuseCount", 3);
        when(sessionRepository.findById(fixture.session.getId())).thenReturn(Optional.of(fixture.session));
        when(memberRepository.findById(fixture.member.getId())).thenReturn(Optional.of(fixture.member));
        when(attendanceRepository.existsBySessionAndCohortMemberMemberId(fixture.session, fixture.member.getId())).thenReturn(false);
        when(currentCohortService.getCurrentCohortNumber()).thenReturn(11);
        when(cohortMemberRepository.findByCohortGenerationAndMemberId(11, fixture.member.getId())).thenReturn(Optional.of(fixture.cohortMember));

        assertThatThrownBy(() -> attendanceService.createAttendance(new AdminAttendanceCreateRequest(fixture.session.getId(), fixture.member.getId(), AttendanceStatus.EXCUSED, null, "병가")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("EXCUSED에서 PRESENT로 바꾸면 excuseCount가 감소한다")
    void updateAttendanceFromExcusedDecreasesExcuseCount() {
        Attendance attendance = existingAttendance(1L, AttendanceStatus.EXCUSED, null, 0);
        ReflectionTestUtils.setField(attendance.getCohortMember(), "excuseCount", 2);
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(attendance));

        attendanceService.updateAttendance(1L, new AdminAttendanceUpdateRequest(AttendanceStatus.PRESENT, null, null));

        assertThat(attendance.getCohortMember().getExcuseCount()).isEqualTo(1);
    }

    private AttendanceService serviceWithClock(Instant instant) {
        return new AttendanceService(
                attendanceRepository,
                sessionRepository,
                qrCodeRepository,
                memberRepository,
                cohortMemberRepository,
                currentCohortService,
                penaltyCalculator,
                depositService,
                excusePolicy,
                Clock.fixed(instant, ZoneId.of("Asia/Seoul"))
        );
    }

    private void stubCheckInFlow(TestFixture fixture) {
        stubCheckInFlow(attendanceService, fixture);
    }

    private void stubCheckInFlow(AttendanceService service, TestFixture fixture) {
        when(qrCodeRepository.findByHashValue("hash")).thenReturn(Optional.of(fixture.qrCode));
        when(memberRepository.findById(fixture.member.getId())).thenReturn(Optional.of(fixture.member));
        when(attendanceRepository.existsBySessionAndCohortMemberMemberId(fixture.session, fixture.member.getId())).thenReturn(false);
        when(currentCohortService.getCurrentCohortNumber()).thenReturn(11);
        when(cohortMemberRepository.findByCohortGenerationAndMemberId(11, fixture.member.getId())).thenReturn(Optional.of(fixture.cohortMember));
    }

    private TestFixture fixture(SessionStatus sessionStatus, Instant qrExpiresAt) {
        Cohort cohort = new Cohort(11, "11기", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(cohort, "id", 2L);
        Part part = new Part(cohort, "SERVER");
        ReflectionTestUtils.setField(part, "id", 6L);
        Member member = new Member("user1", "encoded", "홍길동", "010-1234-5678", MemberRole.MEMBER, MemberStatus.ACTIVE);
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(member, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        CohortMember cohortMember = new CohortMember(cohort, member, part, null, 100000, 0);
        ReflectionTestUtils.setField(cohortMember, "id", 10L);
        Session session = new Session(cohort, "정기 모임", LocalDate.of(2026, 3, 1), LocalTime.of(14, 0), "강남", sessionStatus);
        ReflectionTestUtils.setField(session, "id", 100L);
        ReflectionTestUtils.setField(session, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(session, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        QrCode qrCode = new QrCode(session, "hash", Instant.parse("2026-03-01T00:00:00Z"), qrExpiresAt);
        ReflectionTestUtils.setField(qrCode, "id", 1000L);
        return new TestFixture(member, cohortMember, session, qrCode);
    }

    private Attendance savedAttendance(Long id, Attendance attendance) {
        ReflectionTestUtils.setField(attendance, "id", id);
        ReflectionTestUtils.setField(attendance, "createdAt", Instant.parse("2026-03-01T05:00:00Z"));
        ReflectionTestUtils.setField(attendance, "updatedAt", Instant.parse("2026-03-01T05:00:00Z"));
        return attendance;
    }

    private Attendance existingAttendance(Long id, AttendanceStatus status, Integer lateMinutes, int penaltyAmount) {
        TestFixture fixture = fixture(SessionStatus.IN_PROGRESS, Instant.parse("2026-03-02T00:00:00Z"));
        Attendance attendance = new Attendance(fixture.session, fixture.cohortMember, status, lateMinutes, penaltyAmount, null, null);
        ReflectionTestUtils.setField(attendance, "id", id);
        ReflectionTestUtils.setField(attendance, "createdAt", Instant.parse("2026-03-01T05:00:00Z"));
        ReflectionTestUtils.setField(attendance, "updatedAt", Instant.parse("2026-03-01T05:00:00Z"));
        return attendance;
    }

    private record TestFixture(Member member, CohortMember cohortMember, Session session, QrCode qrCode) {
    }
}
