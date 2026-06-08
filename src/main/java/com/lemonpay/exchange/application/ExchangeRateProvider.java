package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;

import java.time.LocalDate;

public interface ExchangeRateProvider {

    ExchangeRateSnapshot fetch(
            Currency baseCurrency,
            Currency targetCurrency,
            LocalDate rateDate
    );
}
