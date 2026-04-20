package com.lemonpay.wallet.domain;

import java.util.EnumSet;
import java.util.Set;

public enum WalletStatus {
    ACTIVE {
        @Override
        public Set<WalletStatus> allowedTransitions() {
            return EnumSet.of(FROZEN, CLOSED);
        }
    },
    FROZEN {
        @Override
        public Set<WalletStatus> allowedTransitions() {
            return EnumSet.of(ACTIVE, CLOSED);
        }
    },
    CLOSED {
        @Override
        public Set<WalletStatus> allowedTransitions() {
            return EnumSet.noneOf(WalletStatus.class);
        }
    };

    public abstract Set<WalletStatus> allowedTransitions();

    public boolean canTransitionTo(WalletStatus next) {
        return allowedTransitions().contains(next);
    }

    public void validateTransition(WalletStatus next) {
        if(!canTransitionTo(next)) {
            throw new IllegalStateException(
                    "지갑 상태 전이 불가: %s -> %s".formatted(this.name(), next.name()));
        }
    }

}
