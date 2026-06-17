package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "exchange.rate.scheduler.enabled=false"
})
@Transactional
class ExchangeRateSyncUseCaseFallbackTest {

    @Autowired
    private ExchangeRateSyncUseCase useCase;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Test
    @DisplayName("Provider 실패 시 최신 공식 이력을 기준으로 DB_FALLBACK 이력을 저장하고 master를 갱신한다.")
    void fallbackWithLatestOfficialHistory() {
        // given
        ExchangeRateHistory officialHistory = saveOfficialHistory();

        // when
        ExchangeRateSnapshot snapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        assertThat(snapshot.source()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(snapshot.rateType()).isEqualTo(ExchangeRateType.FALLBACK);
        assertThat(snapshot.roundNo()).isEqualTo(2);
        assertThat(snapshot.rate()).isEqualByComparingTo(officialHistory.getRate());

        ExchangeRateHistory latestHistory = exchangeRateHistoryRepository
                .findLatestByCurrencyPairAndRateDate(Currency.USD, Currency.KRW, officialHistory.getRateDate())
                .orElseThrow();

        assertThat(latestHistory.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(latestHistory.getRateType()).isEqualTo(ExchangeRateType.FALLBACK);
        assertThat(latestHistory.getRoundNo()).isEqualTo(2);
        assertThat(latestHistory.getSourceHistoryId()).isEqualTo(officialHistory.getId());

        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(master.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(master.getRateType()).isEqualTo(ExchangeRateType.FALLBACK);
        assertThat(master.getRoundNo()).isEqualTo(2);
        assertThat(master.getRate()).isEqualByComparingTo(officialHistory.getRate());
    }

    @Test
    @DisplayName("Provider 실패 시 기준 공식 이력이 없으면 fallback하지 않고 예외를 전파한다.")
    void failWhenOfficialHistoryMissing() {
        // when & then
        assertThatThrownBy(() -> useCase.syncExchangeRate(Currency.USD, Currency.KRW))
                .isInstanceOf(ExchangeRateProviderException.class)
                .hasMessage("test provider failure");

        assertThat(exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)).isEmpty();
        assertThat(exchangeRateHistoryRepository.findLatestOfficial(Currency.USD, Currency.KRW)).isEmpty();
    }

    @Test
    @DisplayName("Provider 실패가 반복되면 fallback 이력은 다음 회차로 누적되고 원본 공식 이력을 계속 참조한다.")
    void repeatFallbackIncrementsRoundNo() {
        // given
        ExchangeRateHistory officialHistory = saveOfficialHistory();

        // when
        ExchangeRateSnapshot firstFallback = useCase.syncExchangeRate(Currency.USD, Currency.KRW);
        ExchangeRateSnapshot secondFallback = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        assertThat(firstFallback.roundNo()).isEqualTo(2);
        assertThat(secondFallback.roundNo()).isEqualTo(3);

        ExchangeRateHistory latestHistory = exchangeRateHistoryRepository
                .findLatestByCurrencyPairAndRateDate(Currency.USD, Currency.KRW, officialHistory.getRateDate())
                .orElseThrow();

        assertThat(latestHistory.getRoundNo()).isEqualTo(3);
        assertThat(latestHistory.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(latestHistory.getSourceHistoryId()).isEqualTo(officialHistory.getId());

        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(master.getRoundNo()).isEqualTo(3);
        assertThat(master.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
    }

    private ExchangeRateHistory saveOfficialHistory() {
        return exchangeRateHistoryRepository.save(ExchangeRateHistory.official(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                LocalDate.now(),
                1,
                ExchangeRateSource.API,
                LocalDateTime.now().minusMinutes(10)
        ));
    }

    @TestConfiguration
    static class FailingProviderConfig {

        @Bean
        @Primary
        ExchangeRateProvider failingExchangeRateProvider() {
            return (baseCurrency, targetCurrency, rateDate) -> {
                throw new ExchangeRateProviderException("test provider failure");
            };
        }
    }
}
