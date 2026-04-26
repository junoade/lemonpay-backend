package com.lemonpay.wallet.application;

import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.wallet.domain.WalletBalanceService;
import com.lemonpay.wallet.domain.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletQueryService {

    private final WalletService walletService;
    private final WalletBalanceService walletBalanceService;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional(readOnly = true)
    public WalletQueryResult getWalletBalances(UUID walletId) {
        validateAccess(walletId);
        var walletBalances = walletBalanceService.getWalletBalances(walletId);
        return WalletQueryResult.of(walletId, walletBalances);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntryItem> queryLedgerEntries(
            UUID walletId, Currency currency, Pageable pageable
    ) {
        validateAccess(walletId);
        Page<LedgerEntry> result;
        if (currency == null) {
            result = ledgerEntryRepository.findByWalletIdOrderByIdDesc(walletId, pageable);
        } else {
            result = ledgerEntryRepository.findByWalletIdAndCurrencyOrderByIdDesc(walletId, currency, pageable);
        }

        return result.map(LedgerEntryItem::from);
    }

    private void validateAccess(UUID walletId) {
        UUID userId = UserContextHolder.getUserId();
        walletService.validateWalletAccess(walletId, userId);
    }


}
