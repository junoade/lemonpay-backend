package com.lemonpay.payment.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PaymentTransactionServiceTest {

    private static final String TX_NO = "202605180000000001";
    private static final String IDEMPOTENCY_KEY = "idempotency-key";
    private static final String ORDER_ID = "order-id";

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Nested
    @DisplayName("결제 요청 생성")
    class CreatePending {

        @Test
        @DisplayName("신규 결제 요청이면 PENDING 거래를 저장한다.")
        void createPending_withNewRequest_savesPendingTransaction() {
            // given
            UUID walletId = UUID.randomUUID();
            UUID merchantId = UUID.randomUUID();
            Money amount = Money.won(10_000);

            given(paymentTransactionRepository.findByMerchantIdAndIdempotencyKey(merchantId, IDEMPOTENCY_KEY))
                    .willReturn(Optional.empty());
            given(paymentTransactionRepository.existsByTxNo(TX_NO)).willReturn(false);
            given(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            PaymentTransaction result = paymentTransactionService.createPending(
                    TX_NO,
                    walletId,
                    merchantId,
                    amount,
                    amount,
                    BigDecimal.ONE,
                    IDEMPOTENCY_KEY,
                    ORDER_ID
            );

            // then
            assertThat(result.getTxNo()).isEqualTo(TX_NO);
            assertThat(result.getWalletId()).isEqualTo(walletId);
            assertThat(result.getMerchantId()).isEqualTo(merchantId);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            then(paymentTransactionRepository).should().save(any(PaymentTransaction.class));
        }

        @Test
        @DisplayName("같은 가맹점의 동일 idempotencyKey 요청이면 기존 거래를 반환한다.")
        void createPending_withSameIdempotencyKey_returnsExistingTransaction() {
            // given
            UUID walletId = UUID.randomUUID();
            UUID merchantId = UUID.randomUUID();
            PaymentTransaction existing = createPendingTransaction(walletId, merchantId);

            given(paymentTransactionRepository.findByMerchantIdAndIdempotencyKey(merchantId, IDEMPOTENCY_KEY))
                    .willReturn(Optional.of(existing));

            // when
            PaymentTransaction result = paymentTransactionService.createPending(
                    "202605180000000002",
                    walletId,
                    merchantId,
                    Money.won(10_000),
                    Money.won(10_000),
                    BigDecimal.ONE,
                    IDEMPOTENCY_KEY,
                    ORDER_ID
            );

            // then
            assertThat(result).isSameAs(existing);
            then(paymentTransactionRepository).should(never()).save(any(PaymentTransaction.class));
        }

        @Test
        @DisplayName("이미 존재하는 txNo이면 예외가 발생한다.")
        void createPending_withDuplicatedTxNo_throwsException() {
            // given
            UUID walletId = UUID.randomUUID();
            UUID merchantId = UUID.randomUUID();

            given(paymentTransactionRepository.findByMerchantIdAndIdempotencyKey(merchantId, IDEMPOTENCY_KEY))
                    .willReturn(Optional.empty());
            given(paymentTransactionRepository.existsByTxNo(TX_NO)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> paymentTransactionService.createPending(
                    TX_NO,
                    walletId,
                    merchantId,
                    Money.won(10_000),
                    Money.won(10_000),
                    BigDecimal.ONE,
                    IDEMPOTENCY_KEY,
                    ORDER_ID
            ))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_REQUEST);

            then(paymentTransactionRepository).should(never()).save(any(PaymentTransaction.class));
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class Transition {

        @Test
        @DisplayName("PENDING 거래를 COMPLETED로 변경한다.")
        void complete_withPendingTransaction_changesStatusToCompleted() {
            // given
            PaymentTransaction paymentTransaction = createPendingTransaction(UUID.randomUUID(), UUID.randomUUID());

            given(paymentTransactionRepository.findByTxNo(TX_NO)).willReturn(Optional.of(paymentTransaction));
            given(paymentTransactionRepository.save(paymentTransaction)).willReturn(paymentTransaction);

            // when
            PaymentTransaction result = paymentTransactionService.complete(TX_NO);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.getCompletedAt()).isNotNull();
            then(paymentTransactionRepository).should().save(paymentTransaction);
        }

        @Test
        @DisplayName("PENDING 거래를 CANCELLED로 변경한다.")
        void cancel_withPendingTransaction_changesStatusToCancelled() {
            // given
            PaymentTransaction paymentTransaction = createPendingTransaction(UUID.randomUUID(), UUID.randomUUID());

            given(paymentTransactionRepository.findByTxNo(TX_NO)).willReturn(Optional.of(paymentTransaction));
            given(paymentTransactionRepository.save(paymentTransaction)).willReturn(paymentTransaction);

            // when
            PaymentTransaction result = paymentTransactionService.cancel(TX_NO);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(result.getCancelledAt()).isNotNull();
            then(paymentTransactionRepository).should().save(paymentTransaction);
        }

        @Test
        @DisplayName("존재하지 않는 거래번호이면 예외가 발생한다.")
        void complete_withUnknownTxNo_throwsException() {
            // given
            given(paymentTransactionRepository.findByTxNo(TX_NO)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentTransactionService.complete(TX_NO))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);

            then(paymentTransactionRepository).should(never()).save(any(PaymentTransaction.class));
        }
    }

    private PaymentTransaction createPendingTransaction(UUID walletId, UUID merchantId) {
        return PaymentTransaction.create(
                TX_NO,
                walletId,
                merchantId,
                BigDecimal.valueOf(10_000),
                Currency.KRW,
                BigDecimal.valueOf(10_000),
                Currency.KRW,
                BigDecimal.ONE,
                IDEMPOTENCY_KEY,
                ORDER_ID
        );
    }
}
