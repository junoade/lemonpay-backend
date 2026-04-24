package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletBalanceService {

    private final WalletBalanceRepository walletBalanceRepository;

    @Transactional(readOnly = true)
    public WalletBalance getWalletBalance(UUID walletId, Currency currency) {
        return walletBalanceRepository.findByWalletIdAndCurrency(walletId, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Transactional
    public WalletBalance updateWalletBalance(WalletBalance walletBalance) {
        return walletBalanceRepository.save(walletBalance);
    }

    @Transactional(readOnly = true)
    public List<WalletBalance> getWalletBalances(UUID walletId) {
        List<WalletBalance> walletBalances = walletBalanceRepository.findAllByWalletId(walletId);
        if (walletBalances.isEmpty()) {
            throw new RuntimeException("Wallet not found");
        }
        return walletBalances;
    }

}
