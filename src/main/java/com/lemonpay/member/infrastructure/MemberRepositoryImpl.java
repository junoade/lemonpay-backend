package com.lemonpay.member.infrastructure;

import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MemberRepositoryImpl implements MemberRepository {
    private final MemberJpaRepository jpaRepository;

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Member save(Member member) {
        return jpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Transactional
    @Override
    public void closeMember(UUID id) {
        Member member = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));
        member.close();
    }
}
