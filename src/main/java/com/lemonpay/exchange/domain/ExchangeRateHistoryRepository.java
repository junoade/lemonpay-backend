package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;

import java.util.Optional;

public interface ExchangeRateHistoryRepository {

    ExchangeRateHistory save(ExchangeRateHistory history);

    Optional<ExchangeRateHistory> findById(Long id);

    Optional<ExchangeRateHistory> findLatestOfficial(Currency baseCurrency, Currency targetCurrency);
}
