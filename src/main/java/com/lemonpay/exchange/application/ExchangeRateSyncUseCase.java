package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.port.outbound.ExchangeRateProvider;
import com.lemonpay.exchange.application.port.outbound.ExchangeRateProviderException;
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
    private final ExchangeRateHistoryAppender exchangeRateHistoryAppender;
    private final ExchangeRateSyncPolicy syncPolicy;
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Transactional
    public ExchangeRateSnapshot syncExchangeRate(Currency baseCurrency, Currency targetCurrency) {
        LocalDate baseDate = LocalDate.now();

        try {
            ExchangeRateSnapshot fetchedSnapshot = exchangeRateProvider.fetch(baseCurrency, targetCurrency, baseDate);

            ExchangeRateHistory savedHistory = exchangeRateHistoryAppender.appendOfficial(fetchedSnapshot);
            return upsertMasterAndReturn(savedHistory);
        } catch (ExchangeRateProviderException e) {
            ExchangeRateHistory sourceOfficialHistory = exchangeRateHistoryRepository
                    .findLatestOfficial(baseCurrency, targetCurrency)
                    .orElseThrow(() -> e);
            ExchangeRateHistory savedHistory = exchangeRateHistoryAppender.appendDbFallback(sourceOfficialHistory);
            return upsertMasterAndReturn(savedHistory);
        }
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

    private ExchangeRateSnapshot upsertMasterAndReturn(ExchangeRateHistory history) {
        ExchangeRateSnapshot snapshot = ExchangeRateSnapshot.from(history);
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
