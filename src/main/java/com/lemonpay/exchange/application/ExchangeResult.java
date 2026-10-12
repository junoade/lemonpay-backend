package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Money;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeResult(
        UUID walletId,
        Money baseMoney,
        Money targetMoney,
        Money baseBalanceAfter,
        Money targetBalanceAfter,
        BigDecimal appliedRate,
        LocalDate rateDate,
        int roundNo,
        ExchangeRateType rateType,
        ExchangeRateSource rateSource,
        String idempotencyKey
) {
    // TODO
    /*public static ExchangeResult of(
    ) {
        return new ExchangeResult();
    }*/
}
