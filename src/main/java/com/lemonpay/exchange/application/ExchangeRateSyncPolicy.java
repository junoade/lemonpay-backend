package com.lemonpay.exchange.application;

import com.lemonpay.exchange.domain.ExchangeRate;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 어플리케이션 레이어의 환율 동기화 TTL 정책.
 *
 * <p>application 계층이 infra 설정 계층 객체에 의존하지 않도록
 * ExchangeRateConfig에서 수동 Bean으로 등록합니다.</p>
 */
public class ExchangeRateSyncPolicy {
    private final Duration syncTtl;

    public ExchangeRateSyncPolicy(Duration syncTtl) {
        this.syncTtl = syncTtl;
    }

    public boolean isFresh(ExchangeRate rate, LocalDateTime now) {
        return rate.getFetchedAt().isAfter(now.minus(syncTtl));
    }
}
