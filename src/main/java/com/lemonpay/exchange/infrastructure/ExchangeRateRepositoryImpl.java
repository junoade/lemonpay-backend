package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeRateRepositoryImpl implements ExchangeRateRepository {

    private final ExchangeRateJpaRepository jpaRepository;

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        return jpaRepository.save(exchangeRate);
    }

    @Override
    public Optional<ExchangeRate> findByCurrencyPair(Currency baseCurrency, Currency targetCurrency) {
        return jpaRepository.findByBaseCurrencyAndTargetCurrency(baseCurrency, targetCurrency);
    }
}
