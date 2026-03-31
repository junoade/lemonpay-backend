package com.lemonpay.ledger.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID walletId;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Direction direction; // CREDIT / DEBIT

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    private EntryType entryType;

    public static LedgerEntry of(
            UUID walletId,
            Money amount,
            Direction direction,
            Money balanceAfter,
            EntryType entryType
    ) {
        return new LedgerEntry(
                walletId,
                amount.currency(),
                amount.amount(),
                direction,
                balanceAfter.amount(),
                entryType
        );
    }

    private LedgerEntry(UUID walletId, Currency currency, BigDecimal amount,
                        Direction direction, BigDecimal balanceAfter, EntryType entryType) {
        this.walletId = walletId;
        this.currency = currency;
        this.amount = amount;
        this.direction = direction;
        this.balanceAfter = balanceAfter;
        this.entryType = entryType;
    }
}
