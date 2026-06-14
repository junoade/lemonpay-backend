package com.lemonpay.exchange.application;

import java.util.Objects;

public record ExchangeRateSyncResult(
        ExchangeRateSyncStatus status,
        ExchangeRateSnapshot snapshot,
        String reason
) {

    public ExchangeRateSyncResult {
        Objects.requireNonNull(status, "환율 동기화 상태는 필수입니다.");
        Objects.requireNonNull(snapshot, "환율 스냅샷은 필수입니다.");
        Objects.requireNonNull(reason, "환율 동기화 사유는 필수입니다.");
    }

    public static ExchangeRateSyncResult synced(ExchangeRateSnapshot snapshot) {
        return new ExchangeRateSyncResult(ExchangeRateSyncStatus.SYNCED,
                snapshot,
                ExchangeRateSyncStatus.SYNCED.name()
        );
    }

    public static ExchangeRateSyncResult skipped(ExchangeRateSnapshot snapshot,
                                                 String reason) {
        return new ExchangeRateSyncResult(ExchangeRateSyncStatus.SKIPPED, snapshot, reason);
    }

}
