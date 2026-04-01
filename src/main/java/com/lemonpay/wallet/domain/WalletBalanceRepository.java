package com.lemonpay.wallet.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceRepository {

    WalletBalance save(WalletBalance walletBalance);

    Optional<WalletBalance> findByWalletIdAndCurrency(UUID walletId, String currency);

    List<WalletBalance> findAllByWalletId(UUID walletId);
}
