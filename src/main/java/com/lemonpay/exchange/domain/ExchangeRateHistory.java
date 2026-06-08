package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "exchange_rate_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exchange_rate_history_snapshot",
                        columnNames = {
                                "base_currency",
                                "target_currency",
                                "rate_date",
                                "round_no",
                                "rate_type"
                        }
                )
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRateHistory {

    private static final int RATE_SCALE = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 3)
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_currency", nullable = false, length = 3)
    private Currency targetCurrency;

    @Column(name = "rate", nullable = false, precision = 18, scale = RATE_SCALE)
    private BigDecimal rate;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "round_no", nullable = false)
    private int roundNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type", nullable = false, length = 20)
    private ExchangeRateType rateType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ExchangeRateSource source;

    @Column(name = "source_history_id")
    private Long sourceHistoryId;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ExchangeRateHistory(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            int roundNo,
            ExchangeRateType rateType,
            ExchangeRateSource source,
            Long sourceHistoryId,
            LocalDateTime fetchedAt
    ) {
        validateCurrencyPair(baseCurrency, targetCurrency);
        validateRoundNo(roundNo);
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = normalizeRate(rate);
        this.rateDate = Objects.requireNonNull(rateDate, "환율 기준일은 필수입니다.");
        this.roundNo = roundNo;
        this.rateType = Objects.requireNonNull(rateType, "환율 유형은 필수입니다.");
        this.source = Objects.requireNonNull(source, "환율 출처는 필수입니다.");
        this.sourceHistoryId = sourceHistoryId;
        this.fetchedAt = Objects.requireNonNull(fetchedAt, "환율 조회 시각은 필수입니다.");
    }

    public static ExchangeRateHistory official(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            int roundNo,
            ExchangeRateSource source,
            LocalDateTime fetchedAt
    ) {
        return new ExchangeRateHistory(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                roundNo,
                ExchangeRateType.OFFICIAL,
                source,
                null,
                fetchedAt
        );
    }

    public static ExchangeRateHistory provisional(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            int roundNo,
            Long sourceHistoryId,
            LocalDateTime fetchedAt
    ) {
        Objects.requireNonNull(sourceHistoryId, "가환율 원본 이력 ID는 필수입니다.");
        return new ExchangeRateHistory(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                roundNo,
                ExchangeRateType.PROVISIONAL,
                ExchangeRateSource.DB_FALLBACK,
                sourceHistoryId,
                fetchedAt
        );
    }

    private static void validateCurrencyPair(Currency baseCurrency, Currency targetCurrency) {
        Objects.requireNonNull(baseCurrency, "기준 통화는 필수입니다.");
        Objects.requireNonNull(targetCurrency, "대상 통화는 필수입니다.");
        if (baseCurrency == targetCurrency) {
            throw new IllegalArgumentException("기준 통화와 대상 통화는 달라야 합니다.");
        }
    }

    private static void validateRoundNo(int roundNo) {
        if (roundNo <= 0) {
            throw new IllegalArgumentException("환율 회차는 1 이상이어야 합니다.");
        }
    }

    private static BigDecimal normalizeRate(BigDecimal rate) {
        Objects.requireNonNull(rate, "환율은 필수입니다.");
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("환율은 0보다 커야 합니다.");
        }
        return rate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }
}
