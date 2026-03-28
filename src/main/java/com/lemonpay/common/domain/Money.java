package com.lemonpay.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "금액은 필수 입력 입니다.");
        Objects.requireNonNull(currency, "통화는 필수 입력 입니다.");

        if(amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("음수 금액은 허용되지 않습니다." + amount);
        }

        amount = amount.setScale(currency.getScale(), RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------------------
    // 정적 팩토리 메소드 정의
    // ------------------------------------------------------------------------------
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(long amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.KRW);
    }

    public static Money usd(String amount) {
        return new Money(new BigDecimal(amount), Currency.USD);
    }

    public static Money jpy(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.JPY);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    // ------------------------------------------------------------------------------
    // 연산 정의
    // ------------------------------------------------------------------------------
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * <p>equals 재정의
     * record 기본 equals는 BigDecimal.equals()를 사용하는데,
     * 이는 scale까지 비교하므로 1.0 ≠ 1.00 이 됨.
     * compareTo 기반으로 재정의하여 값 동등성을 보장한다.
     * </p>
     * @param o   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.currency == other.currency
                && this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros().hashCode(), currency);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(currency, amount.toPlainString());
    }


    // ------------------------------------------------------------------------------
    // private 메소드 정의
    // ------------------------------------------------------------------------------
    private void validateSameCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "통화 불일치: %s vs %s".formatted(this.currency, other.currency)
            );
        }
    }
}
