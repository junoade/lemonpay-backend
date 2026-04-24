package com.lemonpay.wallet.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.ledger.domain.Direction;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerEntryItem(
        Long id,
        Currency currency,       // domain enum은 그대로 써도 됨
        BigDecimal amount,
        Direction direction,
        EntryType entryType,
        BigDecimal balanceAfter,
        LocalDateTime createdAt
) {
    public static LedgerEntryItem from(LedgerEntry ledgerEntry) {
        return new LedgerEntryItem(
                ledgerEntry.getId(),
                ledgerEntry.getCurrency(),
                ledgerEntry.getAmount(),
                ledgerEntry.getDirection(),
                ledgerEntry.getEntryType(),
                ledgerEntry.getBalanceAfter(),
                ledgerEntry.getCreatedAt()
        );
    }
}
