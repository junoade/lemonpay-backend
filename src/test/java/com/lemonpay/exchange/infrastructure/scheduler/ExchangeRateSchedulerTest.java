package com.lemonpay.exchange.infrastructure.scheduler;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import com.lemonpay.exchange.application.ExchangeRateSyncResult;
import com.lemonpay.exchange.application.ExchangeRateSyncUseCase;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import com.lemonpay.exchange.infrastructure.config.ExchangeRateSchedulerProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateSchedulerTest {

    @Mock
    private ExchangeRateSyncUseCase syncUseCase;

    @Test
    @DisplayName("설정된 통화쌍마다 조건부 환율 동기화를 호출한다.")
    void syncExchangeRate_callUseCaseForEachPair() {
        ExchangeRateSchedulerProperties properties =
                new ExchangeRateSchedulerProperties(
                        true,
                        "0 */10 * * * *",
                        List.of("USD/KRW", "JPY/KRW")
                );

        given(syncUseCase.syncIfStale(any(), any()))
                .willReturn(ExchangeRateSyncResult.synced(mockSnapshot()));

        ExchangeRateScheduler scheduler = new ExchangeRateScheduler(properties, syncUseCase);

        scheduler.syncExchangeRate();

        verify(syncUseCase).syncIfStale(Currency.USD, Currency.KRW);
        verify(syncUseCase).syncIfStale(Currency.JPY, Currency.KRW);

    }

    private ExchangeRateSnapshot mockSnapshot() {
        return new ExchangeRateSnapshot(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                LocalDate.now(),
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }
}