package com.lemonpay.wallet.application;

import com.lemonpay.wallet.domain.WalletBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletQueryService {

    private final WalletBalanceService walletBalanceService;

    @Transactional(readOnly = true)
    public WalletQueryResult getWalletBalances(UUID walletId) {
        var walletBalances = walletBalanceService.getWalletBalances(walletId);
        return WalletQueryResult.of(walletId, walletBalances);
    }
}
