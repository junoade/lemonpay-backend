package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.Currency;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceRepository {

    WalletBalance save(WalletBalance walletBalance);

    Optional<WalletBalance> findByWalletIdAndCurrency(UUID walletId, Currency currency);

    List<WalletBalance> findAllByWalletId(UUID walletId);
}
