package com.lemonpay.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository {
    PaymentTransaction save(PaymentTransaction paymentTransaction);
    Optional<PaymentTransaction> findByTxNo(String txNo);
    Optional<PaymentTransaction> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey);
    boolean existsByTxNo(String txNo);
}
