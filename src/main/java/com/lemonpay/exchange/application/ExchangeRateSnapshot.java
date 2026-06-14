package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record ExchangeRateSnapshot(
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate,
        LocalDate rateDate,
        int roundNo,
        ExchangeRateType rateType,
        ExchangeRateSource source,
        LocalDateTime fetchedAt
) {

    public ExchangeRateSnapshot {
        Objects.requireNonNull(baseCurrency, "기준 통화는 필수입니다.");
        Objects.requireNonNull(targetCurrency, "대상 통화는 필수입니다.");
        Objects.requireNonNull(rate, "환율은 필수입니다.");
        Objects.requireNonNull(rateDate, "환율 기준일은 필수입니다.");
        Objects.requireNonNull(rateType, "환율 유형은 필수입니다.");
        Objects.requireNonNull(source, "환율 출처는 필수입니다.");
        Objects.requireNonNull(fetchedAt, "환율 조회 시각은 필수입니다.");
    }

    public ExchangeRate toExchangeRate() {
        return ExchangeRate.create(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                roundNo,
                rateType,
                source,
                fetchedAt
        );
    }

    public ExchangeRateHistory toOfficialHistory() {
        if (rateType != ExchangeRateType.OFFICIAL) {
            throw new IllegalStateException("공식 환율 스냅샷만 공식 환율 이력으로 변환할 수 있습니다.");
        }

        return ExchangeRateHistory.official(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                roundNo,
                source,
                fetchedAt
        );
    }

    public static ExchangeRateSnapshot from(ExchangeRate exchangeRate) {
        return new ExchangeRateSnapshot(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                exchangeRate.getRateDate(),
                exchangeRate.getRoundNo(),
                exchangeRate.getRateType(),
                exchangeRate.getSource(),
                exchangeRate.getFetchedAt()
        );
    }
}
