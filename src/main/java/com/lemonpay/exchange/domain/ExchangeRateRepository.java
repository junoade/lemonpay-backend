package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository {

    ExchangeRate save(ExchangeRate exchangeRate);

    Optional<ExchangeRate> findByCurrencyPair(Currency baseCurrency, Currency targetCurrency);

    List<ExchangeRate> findAll();
}
