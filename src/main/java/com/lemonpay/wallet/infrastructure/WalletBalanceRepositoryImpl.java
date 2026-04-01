package com.lemonpay.wallet.infrastructure;

import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WalletBalanceRepositoryImpl implements WalletBalanceRepository {
    private final WalletBalanceRepository walletBalanceRepository;

    @Override
    public WalletBalance save(WalletBalance walletBalance) {
        return walletBalanceRepository.save(walletBalance);
    }

    @Override
    public Optional<WalletBalance> findByWalletIdAndCurrency(UUID walletId, String currency) {
        return walletBalanceRepository.findByWalletIdAndCurrency(walletId, currency);
    }

    @Override
    public List<WalletBalance> findAllByWalletId(UUID walletId) {
        return walletBalanceRepository.findAllByWalletId(walletId);
    }
}
