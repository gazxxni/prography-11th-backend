package com.prography.attendance.domain.member.service;

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
import com.prography.attendance.domain.member.dto.AdminMemberDashboardItem;
import com.prography.attendance.domain.member.dto.AdminMemberDetailResponse;
import com.prography.attendance.domain.member.dto.AdminMemberUpdateRequest;
import com.prography.attendance.domain.member.dto.MemberResponse;
import com.prography.attendance.domain.member.dto.MemberWithdrawalResponse;
import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.repository.MemberRepository;
import com.prography.attendance.global.common.PageResponse;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepositService depositService;

    public MemberService(
            MemberRepository memberRepository,
            CohortRepository cohortRepository,
            PartRepository partRepository,
            TeamRepository teamRepository,
            CohortMemberRepository cohortMemberRepository,
            PasswordEncoder passwordEncoder,
            DepositService depositService
    ) {
        this.memberRepository = memberRepository;
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.depositService = depositService;
    }

    public MemberResponse getMember(Long id) {
        return MemberResponse.from(getMemberEntity(id));
    }

    @Transactional
    public AdminMemberDetailResponse createMember(AdminMemberCreateRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Cohort cohort = getCohort(request.cohortId());
        Part part = getPart(cohort, request.partId());
        Team team = getTeam(cohort, request.teamId());

        Member member = memberRepository.save(new Member(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phone(),
                MemberRole.MEMBER,
                MemberStatus.ACTIVE
        ));

        CohortMember cohortMember = cohortMemberRepository.save(new CohortMember(cohort, member, part, team, 0, 0));
        depositService.applyInitialDeposit(cohortMember);
        return toAdminMemberDetail(member, cohortMember);
    }

    public PageResponse<AdminMemberDashboardItem> getMembers(int page, int size, String searchType, String searchValue, Integer generation, String partName, String teamName, MemberStatus status) {
        List<AdminMemberDashboardItem> filtered = memberRepository.findAll().stream()
                .map(member -> toDashboardItem(member, getLatestCohortMember(member.getId()).orElse(null)))
                .filter(item -> matchesSearch(item, searchType, searchValue))
                .filter(item -> generation == null || Objects.equals(item.generation(), generation))
                .filter(item -> partName == null || Objects.equals(item.partName(), partName))
                .filter(item -> teamName == null || Objects.equals(item.teamName(), teamName))
                .filter(item -> status == null || item.status() == status)
                .sorted(Comparator.comparing(AdminMemberDashboardItem::id))
                .toList();

        PageRequest pageRequest = PageRequest.of(page, size);
        int start = Math.min((int) pageRequest.getOffset(), filtered.size());
        int end = Math.min(start + pageRequest.getPageSize(), filtered.size());
        return PageResponse.from(new PageImpl<>(filtered.subList(start, end), pageRequest, filtered.size()));
    }

    public AdminMemberDetailResponse getAdminMember(Long id) {
        Member member = getMemberEntity(id);
        return toAdminMemberDetail(member, getLatestCohortMember(id).orElse(null));
    }

    @Transactional
    public AdminMemberDetailResponse updateMember(Long id, AdminMemberUpdateRequest request) {
        Member member = getMemberEntity(id);
        member.updateProfile(request.name(), request.phone());

        CohortMember cohortMember = getLatestCohortMember(id).orElse(null);
        if (request.cohortId() != null) {
            Cohort cohort = getCohort(request.cohortId());
            Part part = getPart(cohort, request.partId());
            Team team = getTeam(cohort, request.teamId());
            Optional<CohortMember> existing = cohortMemberRepository.findByCohortAndMember(cohort, member);
            if (existing.isPresent()) {
                cohortMember = existing.get();
                cohortMember.updateAssignment(part, team);
            } else {
                cohortMember = cohortMemberRepository.save(new CohortMember(cohort, member, part, team, 0, 0));
                depositService.applyInitialDeposit(cohortMember);
            }
        }
        return toAdminMemberDetail(member, cohortMember);
    }

    @Transactional
    public MemberWithdrawalResponse deleteMember(Long id) {
        Member member = getMemberEntity(id);
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        member.withdraw();
        return new MemberWithdrawalResponse(member.getId(), member.getLoginId(), member.getName(), member.getStatus(), member.getUpdatedAt());
    }

    public Member getMemberEntity(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Cohort getCohort(Long cohortId) {
        return cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
    }

    private Part getPart(Cohort cohort, Long partId) {
        if (partId == null) {
            return null;
        }
        return partRepository.findByIdAndCohort(partId, cohort)
                .orElseThrow(() -> new BusinessException(ErrorCode.PART_NOT_FOUND));
    }

    private Team getTeam(Cohort cohort, Long teamId) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findByIdAndCohort(teamId, cohort)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }

    private Optional<CohortMember> getLatestCohortMember(Long memberId) {
        return cohortMemberRepository.findFirstByMemberIdOrderByCohortGenerationDesc(memberId);
    }

    private AdminMemberDashboardItem toDashboardItem(Member member, CohortMember cohortMember) {
        return new AdminMemberDashboardItem(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                cohortMember != null ? cohortMember.getCohort().getGeneration() : null,
                cohortMember != null && cohortMember.getPart() != null ? cohortMember.getPart().getName() : null,
                cohortMember != null && cohortMember.getTeam() != null ? cohortMember.getTeam().getName() : null,
                cohortMember != null ? cohortMember.getDeposit() : null,
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    private AdminMemberDetailResponse toAdminMemberDetail(Member member, CohortMember cohortMember) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                cohortMember != null ? cohortMember.getCohort().getGeneration() : null,
                cohortMember != null && cohortMember.getPart() != null ? cohortMember.getPart().getName() : null,
                cohortMember != null && cohortMember.getTeam() != null ? cohortMember.getTeam().getName() : null,
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }

    private boolean matchesSearch(AdminMemberDashboardItem item, String searchType, String searchValue) {
        if (searchType == null || searchValue == null || searchValue.isBlank()) {
            return true;
        }
        return switch (searchType) {
            case "name" -> item.name().contains(searchValue);
            case "loginId" -> item.loginId().contains(searchValue);
            case "phone" -> item.phone().contains(searchValue);
            default -> throw new IllegalArgumentException("Invalid searchType");
        };
    }
}
