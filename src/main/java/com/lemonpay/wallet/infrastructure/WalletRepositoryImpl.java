package com.lemonpay.wallet.infrastructure;

import com.lemonpay.member.domain.Member;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    @Override
    public Wallet save(Wallet wallet) {
        return walletJpaRepository.save(wallet);
    }

    @Override
    public Optional<Wallet> findById(UUID walletId) {
        return walletJpaRepository.findById(walletId);
    }

    @Override
    public Optional<Wallet> findByMember(Member member) {
        return walletJpaRepository.findByMember(member);
    }

    @Override
    public Optional<Wallet> findByMemberId(UUID memberId) {
        return walletJpaRepository.findByMemberId(memberId);
    }

    @Override
    public boolean existsByMember(Member member) {
        return walletJpaRepository.existsByMember(member);
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return walletJpaRepository.existsByMemberId(memberId);
    }
}
