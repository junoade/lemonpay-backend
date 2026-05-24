package com.lemonpay.payment.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTransactionTest {

    @Nested
    @DisplayName("생성 테스트")
    class Creation {

        @Test
        @DisplayName("create() 시 PENDING 상태로 생성된다.")
        void createPaymentTransaction_success() {
            //given & when
            PaymentTransaction paymentTransaction = createPendingTransactionByKrw();

            // then
            assertNotNull(paymentTransaction);
            assertEquals(PaymentStatus.PENDING, paymentTransaction.getStatus());
        }

        @Test
        @DisplayName("create() 시 txNo, walletId, merchantId, amount, currency, idempotencyKey가 저장된다.")
        void createPaymentTransactionCheckParams_success() {
            // given & when
            PaymentTransaction paymentTransaction = createPendingTransactionByKrw();

            // then
            assertNotNull(paymentTransaction);
            assertEquals("202605090000000001", paymentTransaction.getTxNo());
            assertNotNull(paymentTransaction.getWalletId());
            assertNotNull(paymentTransaction.getMerchantId());
            assertEquals(PaymentStatus.PENDING, paymentTransaction.getStatus());
            assertThat(paymentTransaction.getAmount()).isEqualByComparingTo("10000");
            assertEquals(Currency.KRW, paymentTransaction.getCurrency());
            assertThat(paymentTransaction.getSettlementAmount()).isEqualByComparingTo("10000");
            assertEquals(Currency.KRW, paymentTransaction.getSettlementCurrency());
            assertThat(paymentTransaction.getExchangeRate()).isEqualByComparingTo("0");
            assertEquals("idempotency_key", paymentTransaction.getIdempotencyKey());
        }

        @Test
        @DisplayName("create() 시 completedAt, cancelledAt은 null이다.")
        void createPaymentTransactionCheckTimeStamp_success() {
            // given & when
            PaymentTransaction paymentTransaction = createPendingTransactionByKrw();

            // then
            assertNull(paymentTransaction.getCompletedAt());
            assertNull(paymentTransaction.getCancelledAt());
        }


    }

    @Nested
    @DisplayName("상태 전이 메소드 테스트")
    class Transition {
        PaymentTransaction paymentTransaction;

        @BeforeEach
        void setUp() {
            paymentTransaction = createPendingTransactionByKrw();
        }

        @Test
        @DisplayName("complete() 호출 시 PENDING -> COMPLETED 전이되고, completedAt이 기록된다.")
        void completePaymentTransaction_success() {
            // given & when
            paymentTransaction.complete();

            // then
            assertEquals(PaymentStatus.COMPLETED, paymentTransaction.getStatus());
            assertNotNull(paymentTransaction.getCompletedAt());
        }


        @Test
        @DisplayName("cancel() 호출 시 PENDING -> CANCELLED 전이되고, cancelledAt이 기록된다.")
        void cancelPaymentTransaction_success() {
            // given & when
            paymentTransaction.cancel();

            // then
            assertEquals(PaymentStatus.CANCELLED, paymentTransaction.getStatus());
            assertNotNull(paymentTransaction.getCancelledAt());
        }

        @Test
        @DisplayName("failed() 호출 시 PENDING -> FAILED 전이된다.")
        void failedTransition_success() {
            // given & when
            paymentTransaction.failed();

            // then
            assertEquals(PaymentStatus.FAILED, paymentTransaction.getStatus());
        }

        /**
         * PaymentStatusTest에서 모든 전이 케이스를 검증하므로, PaymentTransactionTest에서는 대표 케이스만 검증한다.
         */
        @Test
        @DisplayName("COMPLETED 상태에서는 cancel() 불가하다")
        void invalidTransitionFromCompleted_throwsException() {
            // given
            paymentTransaction.complete();

            // when & then
            assertThatThrownBy(() -> paymentTransaction.cancel())
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        }

        @Test
        @DisplayName("FAILED 상태에서는 complete() 불가하다")
        void invalidTransitionFromFailed_throwsException() {
            // given
            paymentTransaction.failed();

            // when & then
            assertThatThrownBy(() -> paymentTransaction.complete())
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        }

        @Test
        @DisplayName("CANCELLED 상태에서는 complete() 불가하다")
        void invalidTransitionFromCancelled_throwsException() {
            // given
            paymentTransaction.cancel();

            // when & then
            assertThatThrownBy(() -> paymentTransaction.complete())
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
        }




    }

    private PaymentTransaction createPendingTransactionByKrw() {
        return PaymentTransaction.create(
                "202605090000000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(10_000),
                Currency.KRW,
                BigDecimal.valueOf(10_000),
                Currency.KRW,
                BigDecimal.valueOf(0),
                "idempotency_key",
                "ORDER-20260518-TEST-001"
        );
    }
}