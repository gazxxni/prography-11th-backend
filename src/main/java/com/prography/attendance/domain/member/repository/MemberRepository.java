package com.prography.attendance.domain.member.repository;

import com.prography.attendance.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
