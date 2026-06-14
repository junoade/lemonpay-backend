package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import com.lemonpay.exchange.infrastructure.config.ExchangeRateProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeRateSyncUseCase {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    private final ExchangeRateProperties exchangeRateProperties;

    @Transactional
    public ExchangeRateSnapshot syncExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        LocalDate baseDate = LocalDate.now();
        ExchangeRateSnapshot snapshot = exchangeRateProvider.fetch(baseCurrency, targetCurrency, baseDate);

        ExchangeRateHistory exchangeRateHistory = snapshot.toOfficialHistory();
        exchangeRateHistoryRepository.save(exchangeRateHistory);
        upsertExchangeRate(snapshot);

        return snapshot;
    }

    @Transactional
    public ExchangeRateSyncResult syncIfStale(Currency baseCurrency, Currency targetCurrency) {
        return exchangeRateRepository.findByCurrencyPair(baseCurrency, targetCurrency)
                .filter(this::isFresh)
                .map(rate -> ExchangeRateSyncResult.skipped(
                        ExchangeRateSnapshot.from(rate),
                        "환율 정보가 TTL 이내로 skip 합니다."
                ))
                .orElseGet(() -> ExchangeRateSyncResult.synced(
                        syncExchangeRate(baseCurrency, targetCurrency)
                ));
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

    private boolean isFresh(ExchangeRate rate) {
        return rate.getFetchedAt()
                .isAfter(LocalDateTime.now().minusMinutes(exchangeRateProperties.syncTtlMinutes()));
    }
}
