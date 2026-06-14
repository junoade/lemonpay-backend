package com.lemonpay.payment.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentStatus {
    /**
     * 대기
     */
    PENDING {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.of(COMPLETED, FAILED, CANCELLED);
        }
    },
    /**
     * 완료
     */
    COMPLETED {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    },
    /**
     * 실패
     */
    FAILED {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    },
    /**
     * 요청 취소
     */
    CANCELLED {
        @Override
        public Set<PaymentStatus> allowedTransitions() {
            return EnumSet.noneOf(PaymentStatus.class);
        }
    };

    public abstract Set<PaymentStatus> allowedTransitions();

    public boolean canTransitionTo(PaymentStatus next) { return allowedTransitions().contains(next); }

    public void validateTransition(PaymentStatus next) {
        if(!canTransitionTo(next)) {
            throw new CoreException(
                    ErrorType.INVALID_STATE_TRANSITION,
                    "결제 내역 상태 전이 불가 : %s -> %s".formatted(this.name(), next.name())
            );
        }
    }
}
