package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record ExchangeCommand(
        UUID walletId,
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal baseAmount,
        String idempotencyKey
) {
}
