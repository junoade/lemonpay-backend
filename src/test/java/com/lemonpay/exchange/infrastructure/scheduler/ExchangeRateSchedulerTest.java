package com.lemonpay.exchange.infrastructure.scheduler;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import com.lemonpay.exchange.application.ExchangeRateSyncResult;
import com.lemonpay.exchange.application.ExchangeRateSyncUseCase;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import com.lemonpay.exchange.infrastructure.config.ExchangeRateSchedulerProperties;
import com.lemonpay.exchange.infrastructure.metrics.ExchangeRateMetrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateSchedulerTest {

    @Mock
    private ExchangeRateSyncUseCase syncUseCase;

    @Mock
    private ExchangeRateMetrics exchangeRateMetrics;

    @Test
    @DisplayName("설정된 통화쌍마다 조건부 환율 동기화를 호출한다.")
    void syncExchangeRate_callUseCaseForEachPair() {
        ExchangeRateSchedulerProperties properties =
                new ExchangeRateSchedulerProperties(
                        true,
                        "0 */10 * * * *",
                        List.of("USD/KRW", "JPY/KRW")
                );

        given(syncUseCase.syncIfStale(Currency.USD, Currency.KRW))
                .willReturn(ExchangeRateSyncResult.synced(mockSnapshot(Currency.USD, Currency.KRW)));
        given(syncUseCase.syncIfStale(Currency.JPY, Currency.KRW))
                .willReturn(ExchangeRateSyncResult.synced(mockSnapshot(Currency.JPY, Currency.KRW)));

        ExchangeRateScheduler scheduler = new ExchangeRateScheduler(properties, syncUseCase, exchangeRateMetrics);

        scheduler.syncExchangeRate();

        verify(syncUseCase).syncIfStale(Currency.USD, Currency.KRW);
        verify(syncUseCase).syncIfStale(Currency.JPY, Currency.KRW);
        verify(exchangeRateMetrics, times(2)).recordSync(any(ExchangeRateSyncResult.class), any(Duration.class));

    }

    @Test
    @DisplayName("특정 통화쌍 동기화가 실패해도 다음 통화쌍 동기화를 계속 진행한다.")
    void syncExchangeRate_continueWhenOnePairFails() {
        ExchangeRateSchedulerProperties properties =
                new ExchangeRateSchedulerProperties(
                        true,
                        "0 */10 * * * *",
                        List.of("USD/KRW", "JPY/KRW")
                );

        given(syncUseCase.syncIfStale(Currency.USD, Currency.KRW))
                .willThrow(new IllegalStateException("API failure"));
        given(syncUseCase.syncIfStale(Currency.JPY, Currency.KRW))
                .willReturn(ExchangeRateSyncResult.synced(mockSnapshot(Currency.JPY, Currency.KRW)));

        ExchangeRateScheduler scheduler = new ExchangeRateScheduler(properties, syncUseCase, exchangeRateMetrics);

        scheduler.syncExchangeRate();

        verify(syncUseCase).syncIfStale(Currency.USD, Currency.KRW);
        verify(syncUseCase).syncIfStale(Currency.JPY, Currency.KRW);
        verify(exchangeRateMetrics).recordFailure(eq("USD/KRW"), any(Duration.class));
        verify(exchangeRateMetrics).recordSync(any(ExchangeRateSyncResult.class), any(Duration.class));
    }

    private ExchangeRateSnapshot mockSnapshot(Currency baseCurrency, Currency targetCurrency) {
        return new ExchangeRateSnapshot(
                baseCurrency,
                targetCurrency,
                new BigDecimal("1350.00000000"),
                LocalDate.now(),
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }
}
