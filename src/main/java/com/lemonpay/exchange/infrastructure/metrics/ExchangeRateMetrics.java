package com.lemonpay.exchange.infrastructure.metrics;

import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import com.lemonpay.exchange.application.ExchangeRateSyncResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateMetrics {

    private static final String SYNC_METRIC_NAME = "exchange.rate.sync";
    private static final String SYNC_DURATION_METRIC_NAME = "exchange.rate.sync.duration";
    private static final String PAIR_TAG = "pair";
    private static final String STATUS_TAG = "status";
    private static final String FAILED_STATUS = "FAILED";

    private final MeterRegistry meterRegistry;

    public void recordSync(ExchangeRateSyncResult result, Duration duration) {
        try {
            String pair = formatPair(result.snapshot());
            String status = result.status().name();

            incrementSyncCounter(pair, status);
            recordSyncDuration(pair, status, duration);
        } catch (Exception e) {
            log.warn("failed to record metrics {}, caused by", SYNC_METRIC_NAME, e);
        }
    }

    public void recordFailure(String pair, Duration duration) {
        try {
            incrementSyncCounter(pair, FAILED_STATUS);
            recordSyncDuration(pair, FAILED_STATUS, duration);
        } catch (Exception e) {
            log.warn("failed to record metrics {}, caused by", SYNC_METRIC_NAME, e);
        }

    }

    private void incrementSyncCounter(String pair, String status) {
        Counter.builder(SYNC_METRIC_NAME)
                .description("Exchange rate sync execution count")
                .tag(PAIR_TAG, pair)
                .tag(STATUS_TAG, status)
                .register(meterRegistry)
                .increment();
    }

    private void recordSyncDuration(String pair, String status, Duration duration) {
        Timer.builder(SYNC_DURATION_METRIC_NAME)
                .description("Exchange rate sync execution duration")
                .tag(PAIR_TAG, pair)
                .tag(STATUS_TAG, status)
                .register(meterRegistry)
                .record(duration);
    }

    private String formatPair(ExchangeRateSnapshot snapshot) {
        return "%s/%s".formatted(snapshot.baseCurrency(), snapshot.targetCurrency());
    }
}
