package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.*;
import com.lemonpay.exchange.infrastructure.ExchangeRateHistoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "exchange.rate.provider=stub"
})
@Transactional
public class ExchangeRateStubIntegrationTest {

    @Autowired
    private ExchangeRateSyncUseCase useCase;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @Autowired
    private ExchangeRateHistoryJpaRepository exchangeRateHistoryJpaRepository;


    @Test
    @DisplayName("stub provider로 환율을 동기화하면 history 내역은 저장되고 master 원장은 upsert된다.")
    void syncWithStubProvider_success() {
        // given & when
        String expectedUsdToKrw = "1350.00000000";
        ExchangeRateSnapshot snapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        assertThat(snapshot.rate()).isEqualByComparingTo(expectedUsdToKrw);
        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(master.getRate()).isEqualByComparingTo(expectedUsdToKrw);

        ExchangeRateHistory history = exchangeRateHistoryRepository.findLatestOfficial(Currency.USD, Currency.KRW)
                .orElseThrow();

        assertThat(history.getRate()).isEqualByComparingTo(expectedUsdToKrw);
    }

    @Test
    @DisplayName("같은 통화쌍/기준일/회차의 환율 이력을 중복 저장하면 unique 제약 위반이 발생한다.")
    void saveDuplicatedRoundHistory_throwsDataIntegrityViolationException() {
        // given
        LocalDate rateDate = LocalDate.now();
        LocalDateTime fetchedAt = LocalDateTime.now();
        ExchangeRateHistory firstRoundHistory = ExchangeRateHistory.official(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                rateDate,
                1,
                ExchangeRateSource.API,
                fetchedAt
        );
        ExchangeRateHistory duplicatedRoundHistory = ExchangeRateHistory.official(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1360.00000000"),
                rateDate,
                1,
                ExchangeRateSource.API,
                fetchedAt.plusMinutes(10)
        );

        exchangeRateHistoryJpaRepository.saveAndFlush(firstRoundHistory);

        // when & then
        assertThatThrownBy(() -> exchangeRateHistoryJpaRepository.saveAndFlush(duplicatedRoundHistory))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("같은 날짜에 환율을 재동기화하면 다음 회차로 history를 누적하고 master는 최신 회차로 갱신한다.")
    void syncExchangeRate_twiceInSameDate_incrementsRoundNo() {
        // when
        ExchangeRateSnapshot firstSnapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);
        ExchangeRateSnapshot secondSnapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        assertThat(firstSnapshot.roundNo()).isEqualTo(1);
        assertThat(secondSnapshot.roundNo()).isEqualTo(2);

        ExchangeRateHistory latestHistory = exchangeRateHistoryRepository
                .findLatestOfficial(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(latestHistory.getRoundNo()).isEqualTo(2);
        assertThat(latestHistory.getRateDate()).isEqualTo(secondSnapshot.rateDate());

        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(master.getRoundNo()).isEqualTo(2);
        assertThat(master.getRate()).isEqualByComparingTo(secondSnapshot.rate());
    }

}
