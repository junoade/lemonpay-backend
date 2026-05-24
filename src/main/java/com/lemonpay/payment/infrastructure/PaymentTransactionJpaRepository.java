package com.lemonpay.payment.infrastructure;

import com.lemonpay.payment.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByTxNo(String txNo);

    Optional<PaymentTransaction> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey);

    boolean existsByTxNo(String txNo);
}
