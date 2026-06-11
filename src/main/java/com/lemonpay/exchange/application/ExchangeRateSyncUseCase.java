package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeRateSyncUseCase {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Transactional
    public ExchangeRateSnapshot syncExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        LocalDate baseDate = LocalDate.now();
        ExchangeRateSnapshot snapshot = exchangeRateProvider.fetch(baseCurrency, targetCurrency, baseDate);

        ExchangeRateHistory exchangeRateHistory = snapshot.toOfficialHistory();
        exchangeRateHistoryRepository.save(exchangeRateHistory);
        upsertExchangeRate(snapshot);

        return snapshot;
    }

    private ExchangeRate upsertExchangeRate(ExchangeRateSnapshot snapshot) {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrencyPair(snapshot.baseCurrency(), snapshot.targetCurrency())
                .map(found -> {
                            found.update(
                                    snapshot.rate(),
                                    snapshot.rateDate(),
                                    snapshot.roundNo(),
                                    snapshot.rateType(),
                                    snapshot.source(),
                                    snapshot.fetchedAt()
                            );
                            return found;
                        }
                ).orElseGet(snapshot::toExchangeRate);

        return exchangeRateRepository.save(exchangeRate);
    }
}
