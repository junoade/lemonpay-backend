package com.lemonpay.wallet.application;

import com.lemonpay.common.domain.Money;
import com.lemonpay.wallet.domain.WalletBalance;

import java.util.List;
import java.util.UUID;

public record WalletQueryResult(
        UUID walletId,
        List<Money> walletBalances
) {
    public static WalletQueryResult of(UUID walletId, List<WalletBalance> walletBalances) {
        var list = walletBalances.stream().map(WalletBalance::toMoney).toList();
        return new WalletQueryResult(walletId, list);
    }
}
