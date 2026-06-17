package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExchangeRateHistoryAppenderUnitTest {

    @Mock
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;

    @InjectMocks
    private ExchangeRateHistoryAppender appender;

    @Test
    @DisplayName("official append 시 같은 기준일의 최신 official 이력 다음 회차로 저장한다.")
    void appendOfficialWithNextRoundNo() {
        // given
        LocalDate rateDate = LocalDate.now();
        ExchangeRateHistory latestHistory = officialHistory(rateDate, 1);
        ExchangeRateSnapshot snapshot = apiSnapshot(rateDate);

        given(exchangeRateHistoryRepository.findLatestByCurrencyPairAndRateDate(
                Currency.USD,
                Currency.KRW,
                rateDate
        )).willReturn(Optional.of(latestHistory));
        given(exchangeRateHistoryRepository.save(any(ExchangeRateHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ExchangeRateHistory savedNewHistory = appender.appendOfficial(snapshot);

        // then
        assertThat(savedNewHistory.getRoundNo()).isEqualTo(2);
        assertThat(savedNewHistory.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(savedNewHistory.getSource()).isEqualTo(ExchangeRateSource.API);
        assertThat(savedNewHistory.getSourceHistoryId()).isNull();

        verify(exchangeRateHistoryRepository).save(savedNewHistory);
    }

    @Test
    @DisplayName("fallback append 시 원본 공식 이력 ID를 sourceHistoryId로 연결한다.")
    void appendFallbackLinksSourceHistoryId() {
        // given
        LocalDate rateDate = LocalDate.now();
        ExchangeRateHistory sourceOfficialHistory = officialHistory(rateDate, 1);
        setId(sourceOfficialHistory, 1L);

        given(exchangeRateHistoryRepository.findLatestByCurrencyPairAndRateDate(
                Currency.USD,
                Currency.KRW,
                rateDate
        )).willReturn(Optional.of(sourceOfficialHistory));
        given(exchangeRateHistoryRepository.save(any(ExchangeRateHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ExchangeRateHistory savedHistory = appender.appendDbFallback(sourceOfficialHistory);

        // then
        assertThat(savedHistory.getRoundNo()).isEqualTo(2);
        assertThat(savedHistory.getRateType()).isEqualTo(ExchangeRateType.FALLBACK);
        assertThat(savedHistory.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(savedHistory.getSourceHistoryId()).isEqualTo(1L);
        assertThat(savedHistory.getRate()).isEqualByComparingTo(sourceOfficialHistory.getRate());

        verify(exchangeRateHistoryRepository).save(savedHistory);
    }

    @Test
    @DisplayName("fallback이 반복되면 최신 fallback 다음 회차로 저장하되 원본 공식 이력 ID을 계속 참조한다.")
    void repeatFallbackKeepsOfficialSourceHistoryId() {
        // given
        LocalDate rateDate = LocalDate.now();
        ExchangeRateHistory sourceOfficialHistory = officialHistory(rateDate, 1);
        setId(sourceOfficialHistory, 1L);

        ExchangeRateHistory latestFallbackHistory = fallbackHistory(sourceOfficialHistory, 2);
        setId(latestFallbackHistory, 2L);

        given(exchangeRateHistoryRepository.findLatestByCurrencyPairAndRateDate(
                Currency.USD,
                Currency.KRW,
                rateDate
        )).willReturn(Optional.of(latestFallbackHistory));
        given(exchangeRateHistoryRepository.save(any(ExchangeRateHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ExchangeRateHistory savedHistory = appender.appendDbFallback(sourceOfficialHistory);

        // then
        assertThat(savedHistory.getRoundNo()).isEqualTo(3);
        assertThat(savedHistory.getRateType()).isEqualTo(ExchangeRateType.FALLBACK);
        assertThat(savedHistory.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
        assertThat(savedHistory.getSourceHistoryId()).isEqualTo(sourceOfficialHistory.getId());
        assertThat(savedHistory.getSourceHistoryId()).isNotEqualTo(latestFallbackHistory.getId());

        verify(exchangeRateHistoryRepository).save(savedHistory);
    }

    private ExchangeRateSnapshot apiSnapshot(LocalDate rateDate) {
        return new ExchangeRateSnapshot(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                rateDate,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }

    private ExchangeRateHistory officialHistory(LocalDate rateDate, int roundNo) {
        return ExchangeRateHistory.official(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                rateDate,
                roundNo,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }

    private ExchangeRateHistory fallbackHistory(ExchangeRateHistory sourceHistory, int roundNo) {
        return ExchangeRateHistory.fallback(
                sourceHistory.getBaseCurrency(),
                sourceHistory.getTargetCurrency(),
                sourceHistory.getRate(),
                sourceHistory.getRateDate(),
                roundNo,
                sourceHistory.getId(),
                LocalDateTime.now()
        );
    }

    /**
     * 단위 테스트에서는 JPA id가 자동 할당되지 않으므로 sourceHistoryId 검증을 위해 id를 세팅한다.
     * 도메인 규칙을 우회하지 않도록 남용을 주의할 것.
     */
    private void setId(ExchangeRateHistory history, Long id) {
        ReflectionTestUtils.setField(history, "id", id);
    }
}
