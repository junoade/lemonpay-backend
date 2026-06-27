package com.lemonpay.exchange.application;

import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 환율 이력 append 전용 컴포넌트.
 * 트랜잭션 경계는 ExchangeRateSyncUseCase에서 관리한다.
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateHistoryAppender {
    private final ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    public ExchangeRateHistory appendOfficial(ExchangeRateSnapshot snapshot) {
        Optional<ExchangeRateHistory> latestHistory = exchangeRateHistoryRepository.findLatestByCurrencyPairAndRateDate(
                snapshot.baseCurrency(),
                snapshot.targetCurrency(),
                snapshot.rateDate()
        );
        int nextRoundNo = nextRoundNo(latestHistory);
        ExchangeRateHistory history = snapshot
                .withRoundNo(nextRoundNo)
                .toOfficialHistory();
        return exchangeRateHistoryRepository.save(history);
    }

    /**
     * fallback이 반복되면 source history 이력이 달라질 수 있으므로 파라미터로 넘기도록 한다.
     * @param officialHistory
     * @return
     */
    public ExchangeRateHistory appendDbFallback(ExchangeRateHistory officialHistory) {
        /*
         * DB_FALLBACK policy:
         * - rateDate는 장애 발생일이 아니라 원본 official history의 기준일을 유지한다.
         * - roundNo는 원본 rateDate의 최신 이력 기준으로 증가시킨다.
         * - fetchedAt은 fallback 생성 시각을 기록한다.
         * - sourceHistoryId는 fallback chain 방지를 위해 항상 원본 official history를 참조한다.
         */
        Optional<ExchangeRateHistory> latestHistory = exchangeRateHistoryRepository.findLatestByCurrencyPairAndRateDate(
                officialHistory.getBaseCurrency(),
                officialHistory.getTargetCurrency(),
                officialHistory.getRateDate()
        );
        int nextRoundNo = nextRoundNo(latestHistory);

        ExchangeRateHistory history = ExchangeRateHistory.fallback(
                officialHistory.getBaseCurrency(),
                officialHistory.getTargetCurrency(),
                officialHistory.getRate(),
                officialHistory.getRateDate(),
                nextRoundNo,
                officialHistory.getId(),
                LocalDateTime.now()
        );

        return exchangeRateHistoryRepository.save(history);
    }


    private int nextRoundNo(Optional<ExchangeRateHistory> latest) {
        return latest
                .map(history -> history.getRoundNo() + 1)
                .orElse(1);
    }
}
