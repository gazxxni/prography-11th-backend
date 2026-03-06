package com.prography.attendance.domain.attendance.service;

import com.prography.attendance.domain.attendance.dto.AdminAttendanceCreateRequest;
import com.prography.attendance.domain.attendance.dto.AdminAttendanceUpdateRequest;
import com.prography.attendance.domain.attendance.dto.AttendanceCheckRequest;
import com.prography.attendance.domain.attendance.dto.AttendanceResponse;
import com.prography.attendance.domain.attendance.dto.AttendanceSummaryResponse;
import com.prography.attendance.domain.attendance.dto.MemberAttendanceDetailResponse;
import com.prography.attendance.domain.attendance.dto.MyAttendanceResponse;
import com.prography.attendance.domain.attendance.dto.SessionAttendanceDetailResponse;
import com.prography.attendance.domain.attendance.dto.SessionAttendanceSummaryItem;
import com.prography.attendance.domain.attendance.entity.Attendance;
import com.prography.attendance.domain.attendance.entity.AttendanceStatus;
import com.prography.attendance.domain.attendance.repository.AttendanceRepository;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.cohort.repository.CohortMemberRepository;
import com.prography.attendance.domain.cohort.service.CurrentCohortService;
import com.prography.attendance.domain.deposit.service.DepositService;
import com.prography.attendance.domain.member.entity.Member;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final QrCodeRepository qrCodeRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final CurrentCohortService currentCohortService;
    private final PenaltyCalculator penaltyCalculator;
    private final DepositService depositService;
    private final ExcusePolicy excusePolicy;
    private final Clock clock;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            SessionRepository sessionRepository,
            QrCodeRepository qrCodeRepository,
            MemberRepository memberRepository,
            CohortMemberRepository cohortMemberRepository,
            CurrentCohortService currentCohortService,
            PenaltyCalculator penaltyCalculator,
            DepositService depositService,
            ExcusePolicy excusePolicy,
            Clock clock
    ) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.memberRepository = memberRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.currentCohortService = currentCohortService;
        this.penaltyCalculator = penaltyCalculator;
        this.depositService = depositService;
        this.excusePolicy = excusePolicy;
        this.clock = clock;
    }

    @Transactional
    public AttendanceResponse checkAttendance(AttendanceCheckRequest request) {
        Instant now = Instant.now(clock);
        QrCode qrCode = qrCodeRepository.findByHashValue(request.hashValue())
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_INVALID));
        if (qrCode.getExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.QR_EXPIRED);
        }
        Session session = qrCode.getSession();
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }
        if (attendanceRepository.existsBySessionAndCohortMemberMemberId(session, member.getId())) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }
        CohortMember cohortMember = cohortMemberRepository.findByCohortGenerationAndMemberId(currentCohortService.getCurrentCohortNumber(), member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        ZonedDateTime nowInZone = ZonedDateTime.now(clock);
        ZonedDateTime scheduled = LocalDateTime.of(session.getDate(), session.getTime()).atZone(nowInZone.getZone());
        AttendanceStatus status = nowInZone.isAfter(scheduled) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        Integer lateMinutes = status == AttendanceStatus.LATE ? Math.toIntExact(ChronoUnit.MINUTES.between(scheduled, nowInZone)) : null;
        int penaltyAmount = penaltyCalculator.calculate(status, lateMinutes);

        Attendance attendance = attendanceRepository.save(new Attendance(session, cohortMember, status, lateMinutes, penaltyAmount, null, now));
        if (penaltyAmount > 0) {
            depositService.deductPenalty(cohortMember, penaltyAmount, attendance);
        }
        return AttendanceResponse.from(attendance);
    }

    public List<MyAttendanceResponse> getMyAttendances(Long memberId) {
        CohortMember cohortMember = getCurrentCohortMember(memberId);
        return attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember).stream()
                .map(MyAttendanceResponse::from)
                .toList();
    }

    public AttendanceSummaryResponse getAttendanceSummary(Long memberId) {
        CohortMember cohortMember = cohortMemberRepository.findByCohortGenerationAndMemberId(currentCohortService.getCurrentCohortNumber(), memberId).orElse(null);
        List<Attendance> attendances = cohortMember == null ? List.of() : attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember);
        return new AttendanceSummaryResponse(
                memberId,
                countByStatus(attendances, AttendanceStatus.PRESENT),
                countByStatus(attendances, AttendanceStatus.ABSENT),
                countByStatus(attendances, AttendanceStatus.LATE),
                countByStatus(attendances, AttendanceStatus.EXCUSED),
                attendances.stream().mapToInt(Attendance::getPenaltyAmount).sum(),
                cohortMember != null ? cohortMember.getDeposit() : null
        );
    }

    @Transactional
    public AttendanceResponse createAttendance(AdminAttendanceCreateRequest request) {
        Session session = getSession(request.sessionId());
        Member member = getMember(request.memberId());
        validateMemberActive(member);
        if (attendanceRepository.existsBySessionAndCohortMemberMemberId(session, member.getId())) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }
        CohortMember cohortMember = getCurrentCohortMember(member.getId());
        validateExcuseTransition(null, request.status(), cohortMember);
        int penaltyAmount = penaltyCalculator.calculate(request.status(), request.lateMinutes());
        Attendance attendance = attendanceRepository.save(new Attendance(
                session,
                cohortMember,
                request.status(),
                normalizeLateMinutes(request.status(), request.lateMinutes()),
                penaltyAmount,
                request.reason(),
                null
        ));
        if (penaltyAmount > 0) {
            depositService.deductPenalty(cohortMember, penaltyAmount, attendance);
        }
        return AttendanceResponse.from(attendance);
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long attendanceId, AdminAttendanceUpdateRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));
        CohortMember cohortMember = attendance.getCohortMember();
        AttendanceStatus oldStatus = attendance.getStatus();
        int oldPenalty = attendance.getPenaltyAmount();
        int newPenalty = penaltyCalculator.calculate(request.status(), request.lateMinutes());
        validateExcuseTransition(oldStatus, request.status(), cohortMember);
        int diff = newPenalty - oldPenalty;
        if (diff > 0) {
            depositService.deductPenalty(cohortMember, diff, attendance);
        } else if (diff < 0) {
            depositService.refund(cohortMember, -diff, attendance);
        }
        attendance.update(request.status(), normalizeLateMinutes(request.status(), request.lateMinutes()), newPenalty, request.reason(), attendance.getCheckedInAt());
        return AttendanceResponse.from(attendance);
    }

    public List<SessionAttendanceSummaryItem> getSessionSummary(Long sessionId) {
        getSession(sessionId);
        return cohortMemberRepository.findByCohortOrderByIdAsc(currentCohortService.getCurrentCohort()).stream()
                .map(this::toSessionAttendanceSummary)
                .toList();
    }

    public MemberAttendanceDetailResponse getMemberAttendanceDetail(Long memberId) {
        CohortMember cohortMember = getCurrentCohortMember(memberId);
        return new MemberAttendanceDetailResponse(
                cohortMember.getMember().getId(),
                cohortMember.getMember().getName(),
                cohortMember.getCohort().getGeneration(),
                cohortMember.getPart() != null ? cohortMember.getPart().getName() : null,
                cohortMember.getTeam() != null ? cohortMember.getTeam().getName() : null,
                cohortMember.getDeposit(),
                cohortMember.getExcuseCount(),
                attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember).stream().map(AttendanceResponse::from).toList()
        );
    }

    public SessionAttendanceDetailResponse getSessionAttendances(Long sessionId) {
        Session session = getSession(sessionId);
        return new SessionAttendanceDetailResponse(
                session.getId(),
                session.getTitle(),
                attendanceRepository.findBySessionOrderByIdAsc(session).stream().map(AttendanceResponse::from).toList()
        );
    }

    private SessionAttendanceSummaryItem toSessionAttendanceSummary(CohortMember cohortMember) {
        List<Attendance> attendances = attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember);
        return new SessionAttendanceSummaryItem(
                cohortMember.getMember().getId(),
                cohortMember.getMember().getName(),
                countByStatus(attendances, AttendanceStatus.PRESENT),
                countByStatus(attendances, AttendanceStatus.ABSENT),
                countByStatus(attendances, AttendanceStatus.LATE),
                countByStatus(attendances, AttendanceStatus.EXCUSED),
                attendances.stream().mapToInt(Attendance::getPenaltyAmount).sum(),
                cohortMember.getDeposit()
        );
    }

    private void validateExcuseTransition(AttendanceStatus oldStatus, AttendanceStatus newStatus, CohortMember cohortMember) {
        if (oldStatus != AttendanceStatus.EXCUSED && newStatus == AttendanceStatus.EXCUSED) {
            excusePolicy.toExcused(cohortMember);
        } else if (oldStatus == AttendanceStatus.EXCUSED && newStatus != AttendanceStatus.EXCUSED) {
            excusePolicy.fromExcused(cohortMember);
        }
    }

    private int countByStatus(List<Attendance> attendances, AttendanceStatus status) {
        return (int) attendances.stream().filter(attendance -> attendance.getStatus() == status).count();
    }

    private Integer normalizeLateMinutes(AttendanceStatus status, Integer lateMinutes) {
        return status == AttendanceStatus.LATE ? lateMinutes : null;
    }

    private Session getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private CohortMember getCurrentCohortMember(Long memberId) {
        return cohortMemberRepository.findByCohortGenerationAndMemberId(currentCohortService.getCurrentCohortNumber(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
    }

    private void validateMemberActive(Member member) {
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }
    }
}
