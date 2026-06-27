package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateQueryService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Transactional(readOnly = true)
    public ExchangeRateSnapshot getLatestExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrencyPair(baseCurrency, targetCurrency)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND,
                        "환율 정보를 찾을 수 없습니다: %s/%s ".formatted(baseCurrency, targetCurrency))
                );
        return ExchangeRateSnapshot.from(exchangeRate);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRateSnapshot> getLatestExchangeRates() {
        return exchangeRateRepository.findAll()
                .stream()
                .map(ExchangeRateSnapshot::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ExchangeRateHistory> findLatestOfficialHistory(Currency baseCurrency, Currency targetCurrency) {
        return exchangeRateHistoryRepository.findLatestOfficial(baseCurrency, targetCurrency);
    }

}
