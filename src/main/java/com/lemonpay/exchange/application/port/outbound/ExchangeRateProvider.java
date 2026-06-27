package com.lemonpay.exchange.application.port.outbound;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;

import java.time.LocalDate;

public interface ExchangeRateProvider {

    ExchangeRateSnapshot fetch(
            Currency baseCurrency,
            Currency targetCurrency,
            LocalDate rateDate
    );
}
