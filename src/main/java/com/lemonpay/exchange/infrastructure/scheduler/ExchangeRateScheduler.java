package com.lemonpay.exchange.infrastructure.scheduler;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeRateSyncResult;
import com.lemonpay.exchange.application.ExchangeRateSyncUseCase;
import com.lemonpay.exchange.infrastructure.config.ExchangeRateSchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "exchange.rate.scheduler.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class ExchangeRateScheduler {
    private final ExchangeRateSchedulerProperties properties;
    private final ExchangeRateSyncUseCase syncUseCase;

    @Scheduled(cron = "${exchange.rate.scheduler.cron}")
    public void syncExchangeRate() {
        if(!properties.enabled()) return;

        for(String pairStr : properties.pairs()) {
            CurrencyPair pair = parseToCurrencyPair(pairStr);

            ExchangeRateSyncResult result = syncUseCase.syncIfStale(pair.baseCurrency, pair.targetCurrency);

            log.info("exchange rate sync result: pair={}/{}, status={}, reason={}",
                    pair.baseCurrency(),
                    pair.targetCurrency(),
                    result.status(),
                    result.reason());
        }
    }

    private record CurrencyPair(Currency baseCurrency, Currency targetCurrency) { }

    private CurrencyPair parseToCurrencyPair(String pair) {
        String[] split = pair.split("/");
        if(split.length != 2) {
            throw new IllegalArgumentException("Invalid pair: " + pair);
        }
        Currency base = Currency.valueOf(split[0]);
        Currency target = Currency.valueOf(split[1]);
        return new CurrencyPair(base, target);
    }
}
