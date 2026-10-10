package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "exchange_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeTransaction extends BaseEntity {

    private static final int RATE_SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부전용 식별자

    @Column(name = "wallet_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID walletId; // Wallet.id 참조

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 3)
    private Currency baseCurrency; // 기준통화

    @Column(name = "base_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal baseAmount; // 기준통화 환산금액

    @Enumerated(EnumType.STRING)
    @Column(name = "target_currency", nullable = false, length = 3)
    private Currency targetCurrency; // 대상통화

    @Column(name = "target_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal targetAmount; // 대상통화 환산 금액

    @Column(name = "base_balance_after", nullable = false, precision = 18, scale = 4)
    private BigDecimal baseBalanceAfter; // 기준통화 환전 후 지갑 잔액

    @Column(name = "target_balance_after", nullable = false, precision = 18, scale = 4)
    private BigDecimal targetBalanceAfter; // 대상통화 환전 후 지갑 잔액

    @Column(name = "applied_rate", nullable = false, precision = 18, scale = RATE_SCALE)
    private BigDecimal appliedRate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private ExchangeRateType rateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_source", nullable = false, length = 20)
    private ExchangeRateSource rateSource;


    public static ExchangeTransaction completed(
            UUID walletId,
            String idempotencyKey,
            ExchangeQuote quote,
            ExchangeRate rate,
            Money baseBalanceAfter,
            Money targetBalanceAfter
    ) {
        return new ExchangeTransaction(
                walletId,
                idempotencyKey,
                quote,
                rate,
                baseBalanceAfter,
                targetBalanceAfter
        );
    }

    private ExchangeTransaction(
            UUID walletId,
            String idempotencyKey,
            ExchangeQuote quote,
            ExchangeRate rate,
            Money baseBalanceAfter,
            Money targetBalanceAfter
    ) {
        Objects.requireNonNull(walletId, "지갑 식별자는 필수입니다.");
        Objects.requireNonNull(quote, "환전 예상 금액은 필수입니다.");
        Objects.requireNonNull(rate, "적용 환율은 필수입니다.");
        Objects.requireNonNull(baseBalanceAfter, "기준 통화 잔액은 필수입니다.");
        Objects.requireNonNull(targetBalanceAfter, "대상 통화 잔액은 필수입니다.");

        validateIdempotencyKey(idempotencyKey);

        Money baseMoney = Objects.requireNonNull(quote.baseMoney(), "기준 통화 금액은 필수입니다.");
        Money targetMoney = Objects.requireNonNull(quote.targetMoney(), "대상 통화 금액은 필수입니다.");
        BigDecimal appliedRate = Objects.requireNonNull(quote.appliedRate(), "적용 환율 값은 필수입니다.");


        if (baseMoney.isZero() || targetMoney.isZero()) {
            throw new IllegalArgumentException("환전 금액은 0보다 커야 합니다.");
        }
        if (baseMoney.currency() != rate.getBaseCurrency()
                || targetMoney.currency() != rate.getTargetCurrency()) {
            throw new IllegalArgumentException("견적과 환율의 통화쌍이 일치해야 합니다.");
        }
        if (appliedRate.compareTo(rate.getRate()) != 0) {
            throw new IllegalArgumentException("견적과 환율의 적용값이 일치해야 합니다.");
        }
        if (baseBalanceAfter.currency() != baseMoney.currency()
                || targetBalanceAfter.currency() != targetMoney.currency()) {
            throw new IllegalArgumentException("변경 후 잔액의 통화가 일치해야 합니다.");
        }

        this.walletId = walletId;
        this.idempotencyKey = idempotencyKey;
        this.baseCurrency = baseMoney.currency();
        this.baseAmount = baseMoney.amount();
        this.targetCurrency = targetMoney.currency();
        this.targetAmount = targetMoney.amount();
        this.baseBalanceAfter = baseBalanceAfter.amount();
        this.targetBalanceAfter = targetBalanceAfter.amount();
        this.appliedRate = appliedRate;
        this.rateDate = rate.getRateDate();
        this.roundNo = rate.getRoundNo();
        this.rateType = rate.getRateType();
        this.rateSource = rate.getSource();
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank() || key.length() > 100) {
            throw new IllegalArgumentException("멱등키는 1~100자의 비어 있지 않은 값이어야 합니다.");
        }
    }

}
