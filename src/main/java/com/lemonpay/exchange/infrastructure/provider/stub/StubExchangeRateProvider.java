package com.lemonpay.exchange.infrastructure.provider.stub;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeRateProvider;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "exchange.rate.provider",
        havingValue = "stub"
)
public class StubExchangeRateProvider implements ExchangeRateProvider {

    private static final Map<CurrencyPair, BigDecimal> RATES = Map.of(
            new CurrencyPair(Currency.USD, Currency.KRW), new BigDecimal("1350.00000000"),
            new CurrencyPair(Currency.JPY, Currency.KRW), new BigDecimal("9.20000000")
    );

    @Override
    public ExchangeRateSnapshot fetch(Currency baseCurrency, Currency targetCurrency, LocalDate rateDate) {
        BigDecimal rate = RATES.get(new CurrencyPair(baseCurrency, targetCurrency));
        if (rate == null) {
            throw new IllegalArgumentException(
                    "Stub 환율을 지원하지 않는 통화쌍입니다: %s/%s"
                            .formatted(baseCurrency, targetCurrency)
            );
        }

        return new ExchangeRateSnapshot(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }

    private record CurrencyPair(Currency baseCurrency, Currency targetCurrency) { }
}
