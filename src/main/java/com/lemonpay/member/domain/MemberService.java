package com.lemonpay.member.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "회원 정보를 다시 확인해주세요."));
    }

    @Transactional(readOnly = true)
    public Member validateAccess(String email, String name) {
        Member member = getMemberByEmail(email);
        if (!member.getName().equals(name)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "로그인 정보가 틀렸습니다.");
        }
        return member;
    }

    @Transactional
    public Member joinMember(String email, String name, String phone) {
        if (memberRepository.existsByEmail(email)) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "이미 존재하는 회원정보입니다.");
        }
        return memberRepository.save(Member.create(email, name, phone));
    }
}
