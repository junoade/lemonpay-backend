package com.lemonpay.payment.application;

import com.lemonpay.common.domain.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentCommand {

    public record Create (
            UUID walletId,
            UUID merchantId,
            Currency currency,
            BigDecimal amount,
            Currency settlementCurrency,
            BigDecimal settlementAmount,
            BigDecimal exchangeRate,
            String orderId,
            String idempotencyKey
    ) { }

    public record Approve (
            String txNo
    ) { }

    public record Cancel (
            String txNo

    ) { }

    public record Query (
        String txNo
    ) { }
}
