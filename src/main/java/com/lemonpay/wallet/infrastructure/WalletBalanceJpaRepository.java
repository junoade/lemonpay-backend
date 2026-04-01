package com.lemonpay.wallet.infrastructure;

import com.lemonpay.wallet.domain.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalance, UUID> {
}
