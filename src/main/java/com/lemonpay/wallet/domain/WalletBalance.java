package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "wallet_balance",
uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_wallet_balance_wallet_currency",
                columnNames = {"wallet_id", "currency"}
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletBalance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal balance;

    @Version
    private Long version;

    protected WalletBalance(Wallet wallet, Currency currency, BigDecimal balance) {
        this.wallet = wallet;
        this.currency = currency;
        this.balance = balance;
        this.version = 0L;
    }

    public static WalletBalance zero(Wallet wallet, Currency currency) {
        BigDecimal initBalance = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        return new WalletBalance(wallet, currency, initBalance);
    }

    public Money toMoney() {
        return Money.of(this.balance, this.currency);
    }

    public void increase(Money money) {
        validateCurrency(money);
        this.balance = this.balance.add(money.amount());
    }

    public void decrease(Money money) {
        validateCurrency(money);

        BigDecimal newBalance = this.balance.subtract(money.amount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("잔액 부족");
        }
        this.balance = newBalance;
    }

    private void validateCurrency(Money money) {
        if (this.currency != money.currency()) {
            throw new IllegalArgumentException("통화 불일치");
        }
    }
}
