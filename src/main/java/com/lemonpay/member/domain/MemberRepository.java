package com.lemonpay.member.domain;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
    Member save(Member member);
    Optional<Member> findById(UUID id);
    Optional<Member> findByEmail(String email);
    void closeMember(UUID id);
}
