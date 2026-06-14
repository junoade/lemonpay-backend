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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeRateSyncUseCase {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    private final ExchangeRateSyncPolicy syncPolicy;

    @Transactional
    public ExchangeRateSnapshot syncExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        LocalDate baseDate = LocalDate.now();
        ExchangeRateSnapshot fetchedSnapshot = exchangeRateProvider.fetch(baseCurrency, targetCurrency, baseDate);

        int nextRoundNo = exchangeRateHistoryRepository
                .findLatestOfficial(fetchedSnapshot.baseCurrency(), fetchedSnapshot.targetCurrency())
                .filter(history -> history.getRateDate().isEqual(baseDate))
                .map(history -> history.getRoundNo() + 1)
                .orElse(1);

        ExchangeRateSnapshot snapshot = fetchedSnapshot.withRoundNo(nextRoundNo);
        ExchangeRateHistory exchangeRateHistory = snapshot.toOfficialHistory();

        exchangeRateHistoryRepository.save(exchangeRateHistory);
        upsertExchangeRate(snapshot);

        return snapshot;
    }

    @Transactional
    public ExchangeRateSyncResult syncIfStale(Currency baseCurrency, Currency targetCurrency) {
        LocalDateTime now = LocalDateTime.now();
        return exchangeRateRepository.findByCurrencyPair(baseCurrency, targetCurrency)
                .filter(rate -> syncPolicy.isFresh(rate, now))
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
}
