package com.lemonpay.ledger.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntry, Long> {
    Optional<LedgerEntry> findTopByWalletIdAndCurrencyOrderByIdDesc(UUID walletId, Currency currency);

    Page<LedgerEntry> findByWalletIdOrderByIdDesc(UUID walletId, Pageable pageable);
    Page<LedgerEntry> findByWalletIdAndCurrencyOrderByIdDesc(UUID walletId, Currency currency, Pageable pageable);
    Page<LedgerEntry> findByWalletIdAndCurrencyAndEntryTypeOrderByIdDesc(
            UUID walletId,
            Currency currency,
            EntryType entryType,
            Pageable pageable
    );
}
