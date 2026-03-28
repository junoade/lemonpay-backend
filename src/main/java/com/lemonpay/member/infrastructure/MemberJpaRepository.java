package com.lemonpay.member.infrastructure;

import com.lemonpay.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
}
