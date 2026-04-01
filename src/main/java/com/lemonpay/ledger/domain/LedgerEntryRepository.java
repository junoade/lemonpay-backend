package com.lemonpay.ledger.domain;

import com.lemonpay.common.domain.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository {
    LedgerEntry save(LedgerEntry ledgerEntry);

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
