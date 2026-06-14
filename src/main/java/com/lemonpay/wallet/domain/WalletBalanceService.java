package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
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
                .orElseThrow(() -> new CoreException(ErrorType.WALLET_NOT_FOUND));
    }

    @Transactional
    public WalletBalance updateWalletBalance(WalletBalance walletBalance) {
        return walletBalanceRepository.save(walletBalance);
    }

    @Transactional(readOnly = true)
    public List<WalletBalance> getWalletBalances(UUID walletId) {
        List<WalletBalance> walletBalances = walletBalanceRepository.findAllByWalletId(walletId);
        if (walletBalances.isEmpty()) {
            throw new CoreException(ErrorType.WALLET_NOT_FOUND);
        }
        return walletBalances;
    }

}
