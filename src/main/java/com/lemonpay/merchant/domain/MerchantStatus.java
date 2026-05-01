package com.lemonpay.merchant.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;

import java.util.EnumSet;
import java.util.Set;


public enum MerchantStatus {
    ACTIVE {
        @Override
        public Set<MerchantStatus> allowedTransitions() {
            return EnumSet.of(MerchantStatus.SUSPENDED, MerchantStatus.CLOSED);
        }
    },
    SUSPENDED {
        @Override
        public Set<MerchantStatus> allowedTransitions() {
            return EnumSet.of(MerchantStatus.ACTIVE, MerchantStatus.CLOSED);
        }
    },
    CLOSED {
        /**
         * 가맹점 해지했다가 재가입하는 경우를 고려하여 CLOSED -> ACTIVE 상태 전이 허용함
         * @return
         */
        @Override
        public Set<MerchantStatus> allowedTransitions() {
            return EnumSet.of(MerchantStatus.ACTIVE);
        }
    };

    public abstract Set<MerchantStatus> allowedTransitions();
    public boolean canTransitionTo(MerchantStatus next) { return allowedTransitions().contains(next); }
    public void validateTransition(MerchantStatus next) {
        if(!canTransitionTo(next)) {
            throw new CoreException(ErrorType.INVALID_STATE_TRANSITION,
                    "가맹점 상태 전이 불가: %s -> %s".formatted(this.name(), next.name()));
        }
    }
}
