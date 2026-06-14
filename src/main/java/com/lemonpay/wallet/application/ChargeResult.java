package com.lemonpay.wallet.application;

import com.lemonpay.common.domain.Money;
import com.lemonpay.wallet.domain.WalletBalance;

import java.util.UUID;

public record ChargeResult(
        UUID walletId,
        Money chargeMoney,
        Money afterBalance
) {
    public static ChargeResult of(UUID walletId, Money money, WalletBalance afterWalletBalance) {
        return new ChargeResult(walletId, money, afterWalletBalance.toMoney());
    }
}
