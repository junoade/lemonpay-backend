package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeRateHistoryRepositoryImpl implements ExchangeRateHistoryRepository {

    private final ExchangeRateHistoryJpaRepository jpaRepository;

    @Override
    public ExchangeRateHistory save(ExchangeRateHistory history) {
        return jpaRepository.save(history);
    }

    @Override
    public Optional<ExchangeRateHistory> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ExchangeRateHistory> findLatestOfficial(Currency baseCurrency, Currency targetCurrency) {
        return jpaRepository.findFirstByBaseCurrencyAndTargetCurrencyAndRateTypeOrderByRateDateDescRoundNoDesc(
                baseCurrency,
                targetCurrency,
                ExchangeRateType.OFFICIAL
        );
    }

    @Override
    public Optional<ExchangeRateHistory> findLatestByCurrencyPairAndRateDate(
            Currency baseCurrency,
            Currency targetCurrency,
            LocalDate rateDate
    ) {
        return jpaRepository.findFirstByBaseCurrencyAndTargetCurrencyAndRateDateOrderByRoundNoDesc(
                baseCurrency,
                targetCurrency,
                rateDate
        );
    }
}
