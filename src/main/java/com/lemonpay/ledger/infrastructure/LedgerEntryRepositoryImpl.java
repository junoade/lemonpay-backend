package com.lemonpay.ledger.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public LedgerEntry save(LedgerEntry ledgerEntry) {
        return ledgerEntryJpaRepository.save(ledgerEntry);
    }

    @Override
    public Optional<LedgerEntry> findTopByWalletIdAndCurrencyOrderByIdDesc(UUID walletId, Currency currency) {
        return ledgerEntryJpaRepository.findTopByWalletIdAndCurrencyOrderByIdDesc(walletId, currency);
    }

    @Override
    public Page<LedgerEntry> findByWalletIdOrderByIdDesc(UUID walletId, Pageable pageable) {
        return ledgerEntryJpaRepository.findByWalletIdOrderByIdDesc(walletId, pageable);
    }

    @Override
    public Page<LedgerEntry> findByWalletIdAndCurrencyOrderByIdDesc(UUID walletId, Currency currency, Pageable pageable) {
        return ledgerEntryJpaRepository.findByWalletIdAndCurrencyOrderByIdDesc(walletId, currency, pageable);
    }

    @Override
    public Page<LedgerEntry> findByWalletIdAndCurrencyAndEntryTypeOrderByIdDesc(UUID walletId, Currency currency, EntryType entryType, Pageable pageable) {
        return ledgerEntryJpaRepository.findByWalletIdAndCurrencyAndEntryTypeOrderByIdDesc(
                walletId, currency, entryType, pageable
        );
    }
}
