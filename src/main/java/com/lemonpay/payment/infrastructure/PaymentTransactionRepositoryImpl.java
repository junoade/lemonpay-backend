package com.lemonpay.payment.infrastructure;

import com.lemonpay.payment.domain.PaymentTransaction;
import com.lemonpay.payment.domain.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepository {
    private final PaymentTransactionJpaRepository jpaRepository;

    @Override
    public PaymentTransaction save(PaymentTransaction paymentTransaction) {
        return jpaRepository.save(paymentTransaction);
    }

    @Override
    public Optional<PaymentTransaction> findByTxNo(String txNo) {
        return jpaRepository.findByTxNo((txNo));
    }

    @Override
    public Optional<PaymentTransaction> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey) {
        return jpaRepository.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey);
    }

    @Override
    public boolean existsByTxNo(String txNo) {
        return jpaRepository.existsByTxNo(txNo);
    }
}
