package com.lemonpay.wallet.infrastructure;

import com.lemonpay.member.domain.Member;
import com.lemonpay.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByMemberId(UUID memberId);
    Optional<Wallet> findByMember(Member member);
    boolean existsByMemberId(UUID memberId);
    boolean existsByMember(Member member);
    boolean existsByIdAndMemberId(UUID walletId, UUID memberId);
}
