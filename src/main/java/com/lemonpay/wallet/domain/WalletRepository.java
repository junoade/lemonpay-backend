package com.lemonpay.wallet.domain;

import com.lemonpay.member.domain.Member;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(UUID walletId);
    Optional<Wallet> findByMember(Member member);
    Optional<Wallet> findByMemberId(UUID memberId);

    boolean existsByMember(Member member);
    boolean existsByMemberId(UUID memberId);
    boolean existsByIdAndMemberId(UUID walletId, UUID memberId);
}
