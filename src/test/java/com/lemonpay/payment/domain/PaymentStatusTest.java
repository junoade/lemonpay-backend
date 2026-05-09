package com.lemonpay.payment.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


/**
 * 상태 전이 규칙을 검증합니다.
 */
class PaymentStatusTest {

    @Test
    @DisplayName("PENDING 상태에서 COMPLETED, FAILED, CANCELLED 로 상태 전이가 가능하다.")
    void transitionFromPending_thenSuccess() {
        // given
        PaymentStatus status = PaymentStatus.PENDING;

        // when & then
        assertAll(
                () -> assertTrue(status.canTransitionTo(PaymentStatus.COMPLETED)),
                () -> assertTrue(status.canTransitionTo(PaymentStatus.FAILED)),
                () -> assertTrue(status.canTransitionTo(PaymentStatus.CANCELLED))
        );

    }

    @Test
    @DisplayName("COMPLETED, FAILED, CANCELLED 인 경우 다른 상태로 전이가 불가능하다.")
    void transitionFromTerminal_thenUnable() {
        // given
        PaymentStatus completed = PaymentStatus.COMPLETED;
        PaymentStatus failed = PaymentStatus.FAILED;
        PaymentStatus cancelled = PaymentStatus.CANCELLED;

        // when & then
        assertAll(
                () -> assertTrue(completed.allowedTransitions().isEmpty()),
                () -> assertTrue(failed.allowedTransitions().isEmpty()),
                () -> assertTrue(cancelled.allowedTransitions().isEmpty())
        );
    }


    @ParameterizedTest
    @MethodSource("invalidTransitions")
    @DisplayName("불가능한 전이는 INVALID_STATE_TRANSITION 에러 타입으로 CoreException이 발생한다.")
    void invalidTransition_throwsException(PaymentStatus current, PaymentStatus next) {
        // when & then
        assertThatThrownBy(() -> current.validateTransition(next))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.INVALID_STATE_TRANSITION);
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(PaymentStatus.COMPLETED, PaymentStatus.COMPLETED),
                Arguments.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED),
                Arguments.of(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED),
                Arguments.of(PaymentStatus.COMPLETED, PaymentStatus.PENDING),

                Arguments.of(PaymentStatus.FAILED, PaymentStatus.COMPLETED),
                Arguments.of(PaymentStatus.FAILED, PaymentStatus.FAILED),
                Arguments.of(PaymentStatus.FAILED, PaymentStatus.CANCELLED),
                Arguments.of(PaymentStatus.FAILED, PaymentStatus.PENDING),

                Arguments.of(PaymentStatus.CANCELLED, PaymentStatus.COMPLETED),
                Arguments.of(PaymentStatus.CANCELLED, PaymentStatus.FAILED),
                Arguments.of(PaymentStatus.CANCELLED, PaymentStatus.CANCELLED),
                Arguments.of(PaymentStatus.CANCELLED, PaymentStatus.PENDING)
        );
    }


}