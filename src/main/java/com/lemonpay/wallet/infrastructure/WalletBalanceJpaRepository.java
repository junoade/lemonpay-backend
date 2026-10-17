package com.lemonpay.wallet.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.wallet.domain.WalletBalance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceJpaRepository extends JpaRepository<WalletBalance, UUID> {

    Optional<WalletBalance> findByWalletIdAndCurrency(UUID walletId, Currency currency);

    List<WalletBalance> findAllByWalletId(UUID walletId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("""
        SELECT wb
        FROM WalletBalance wb
        WHERE wb.wallet.id = :walletId
            AND wb.currency = :currency
    """)
    Optional<WalletBalance> findForUpdateByWalletIdAndCurrency(
            @Param("walletId") UUID walletId,
            @Param("currency") Currency currency);
}
