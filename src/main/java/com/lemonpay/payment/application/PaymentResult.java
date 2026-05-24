package com.lemonpay.payment.application;

import com.lemonpay.payment.domain.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResult(
        String txNo,
        String status,
        UUID walletId,
        UUID merchantId,
        String merchantName,
        BigDecimal amount,
        String currency,
        BigDecimal settlementAmount,
        String settlementCurrency,
        BigDecimal exchangeRate,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        LocalDateTime cancelledAt
) {
    public static PaymentResult of(PaymentTransaction tx, String merchantName) {
        return new PaymentResult(
                tx.getTxNo(),
                tx.getStatus().name(),
                tx.getWalletId(),
                tx.getMerchantId(),
                merchantName,
                tx.getAmount(),
                tx.getCurrency().name(),
                tx.getSettlementAmount(),
                tx.getSettlementCurrency().name(),
                tx.getExchangeRate(),
                tx.getCreatedAt(),
                tx.getCompletedAt(),
                tx.getCancelledAt());
    }
}
