package com.lemonpay.payment.domain;

import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public PaymentTransaction createPending(String txNo,
                                            UUID walletId,
                                            UUID merchantId,
                                            Money paymentAmount,
                                            Money settlementAmount,
                                            BigDecimal exchangeRate,
                                            String idempotencyKey,
                                            String orderId) {
        return paymentTransactionRepository
                .findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .orElseGet(() -> createNewPending(
                        txNo,
                        walletId,
                        merchantId,
                        paymentAmount,
                        settlementAmount,
                        exchangeRate,
                        idempotencyKey,
                        orderId
                ));
    }

    @Transactional(readOnly = true)
    public PaymentTransaction getByTxNo(String txNo) {
        return paymentTransactionRepository.findByTxNo(txNo)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 내역을 찾을 수 없습니다."));
    }

    @Transactional
    public PaymentTransaction complete(String txNo) {
        PaymentTransaction paymentTransaction = getByTxNo(txNo);
        paymentTransaction.complete();
        return paymentTransactionRepository.save(paymentTransaction);
    }

    @Transactional
    public PaymentTransaction cancel(String txNo) {
        PaymentTransaction paymentTransaction = getByTxNo(txNo);
        paymentTransaction.cancel();
        return paymentTransactionRepository.save(paymentTransaction);
    }

    @Transactional
    public PaymentTransaction fail(String txNo) {
        PaymentTransaction paymentTransaction = getByTxNo(txNo);
        paymentTransaction.failed();
        return paymentTransactionRepository.save(paymentTransaction);
    }

    @Transactional(readOnly = true)
    public void validateCompletable(PaymentTransaction paymentTransaction) {
        paymentTransaction.getStatus().validateTransition(PaymentStatus.COMPLETED);
    }

    private PaymentTransaction createNewPending(String txNo,
                                                UUID walletId,
                                                UUID merchantId,
                                                Money paymentAmount,
                                                Money settlementAmount,
                                                BigDecimal exchangeRate,
                                                String idempotencyKey,
                                                String orderId) {

        if(paymentTransactionRepository.existsByTxNo(txNo)) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "이미 존재하는 결재 번호 입니다. 다시 확인해주세요.");
        }

        PaymentTransaction paymentTransaction = PaymentTransaction.create(
                txNo,
                walletId,
                merchantId,
                paymentAmount.amount(),
                paymentAmount.currency(),
                settlementAmount.amount(),
                settlementAmount.currency(),
                exchangeRate,
                idempotencyKey,
                orderId
        );

        return paymentTransactionRepository.save(paymentTransaction);
    }
}
