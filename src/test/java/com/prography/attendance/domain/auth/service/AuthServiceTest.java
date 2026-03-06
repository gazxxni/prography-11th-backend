package com.prography.attendance.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.prography.attendance.domain.auth.dto.LoginRequest;
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
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {
        Member member = member(1L, "admin", "admin1234", MemberStatus.ACTIVE);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(member));

        var response = authService.login(new LoginRequest("admin", "admin1234"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.loginId()).isEqualTo("admin");
    }

    @Test
    @DisplayName("비밀번호가 다르면 로그인 실패")
    void loginFailWhenPasswordMismatch() {
        Member member = member(1L, "admin", "admin1234", MemberStatus.ACTIVE);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("탈퇴 회원은 로그인할 수 없다")
    void loginFailWhenWithdrawn() {
        Member member = member(1L, "admin", "admin1234", MemberStatus.WITHDRAWN);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "admin1234")))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_WITHDRAWN);
    }

    private Member member(Long id, String loginId, String rawPassword, MemberStatus status) {
        Member member = new Member(loginId, passwordEncoder.encode(rawPassword), "관리자", "010-0000-0000", MemberRole.ADMIN, status);
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(member, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        return member;
    }
}
