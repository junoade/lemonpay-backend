package com.lemonpay.wallet.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.wallet.domain.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalance, UUID> {

    Optional<WalletBalance> findByWalletIdAndCurrency(UUID walletId, Currency currency);
    List<WalletBalance> findAllByWalletId(UUID walletId);

}
