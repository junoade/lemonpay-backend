package com.lemonpay.exchange.infrastructure.provider.exchangeapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lemonpay.common.domain.Currency;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

record ExchangeRateApiResponse(
        String result,
        @JsonProperty("base_code")
        String baseCode,
        @JsonProperty("conversion_rates")
        Map<String, BigDecimal> conversionRates,
        @JsonProperty("error-type")
        String errorType
) {

    boolean isSuccess() {
        return "success".equals(result);
    }

    Optional<BigDecimal> rateOf(Currency targetCurrency) {
        if (conversionRates == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(conversionRates.get(targetCurrency.name()));
    }
}
