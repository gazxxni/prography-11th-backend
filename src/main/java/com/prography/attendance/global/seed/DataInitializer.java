package com.prography.attendance.global.seed;

import com.prography.attendance.domain.cohort.entity.Cohort;
import com.prography.attendance.domain.cohort.entity.CohortMember;
import com.prography.attendance.domain.cohort.entity.Part;
import com.prography.attendance.domain.cohort.entity.Team;
import com.prography.attendance.domain.cohort.repository.CohortMemberRepository;
import com.prography.attendance.domain.cohort.repository.CohortRepository;
import com.prography.attendance.domain.cohort.repository.PartRepository;
import com.prography.attendance.domain.cohort.repository.TeamRepository;
import com.prography.attendance.domain.deposit.entity.DepositHistory;
import com.prography.attendance.domain.deposit.entity.DepositType;
import com.prography.attendance.domain.deposit.repository.DepositHistoryRepository;
import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberRole;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.repository.MemberRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DataInitializer(
            CohortRepository cohortRepository,
            PartRepository partRepository,
            TeamRepository teamRepository,
            MemberRepository memberRepository,
            CohortMemberRepository cohortMemberRepository,
            DepositHistoryRepository depositHistoryRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositHistoryRepository = depositHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (cohortRepository.count() > 0) {
            return;
        }

        Instant now = Instant.now(clock);
        Cohort cohort10 = cohortRepository.save(new Cohort(10, "10기", now));
        Cohort cohort11 = cohortRepository.save(new Cohort(11, "11기", now));

        List<String> partNames = List.of("SERVER", "WEB", "iOS", "ANDROID", "DESIGN");
        partNames.forEach(name -> partRepository.save(new Part(cohort10, name)));
        partNames.forEach(name -> partRepository.save(new Part(cohort11, name)));

        teamRepository.save(new Team(cohort11, "Team A"));
        teamRepository.save(new Team(cohort11, "Team B"));
        teamRepository.save(new Team(cohort11, "Team C"));

        Member admin = memberRepository.save(new Member(
                "admin",
                passwordEncoder.encode("admin1234"),
                "관리자",
                "010-0000-0000",
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
        ));

        Part serverPart = partRepository.findByCohortOrderByIdAsc(cohort11).stream()
                .filter(part -> "SERVER".equals(part.getName()))
                .findFirst()
                .orElseThrow();

        CohortMember adminCohortMember = cohortMemberRepository.save(
                new CohortMember(cohort11, admin, serverPart, null, 100000, 0)
        );

        depositHistoryRepository.save(new DepositHistory(
                adminCohortMember,
                DepositType.INITIAL,
                100000,
                100000,
                null,
                "초기 보증금",
                now
        ));
    }
}
