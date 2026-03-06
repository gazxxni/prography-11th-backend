package com.prography.attendance.domain.auth.service;

import com.prography.attendance.domain.auth.dto.LoginRequest;
import com.prography.attendance.domain.auth.dto.LoginResponse;
import com.prography.attendance.domain.member.entity.Member;
import com.prography.attendance.domain.member.entity.MemberStatus;
import com.prography.attendance.domain.member.repository.MemberRepository;
import com.prography.attendance.global.error.BusinessException;
import com.prography.attendance.global.error.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }
        return LoginResponse.from(member);
    }
}
