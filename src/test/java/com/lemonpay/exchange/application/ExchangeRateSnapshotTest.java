package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateSnapshotTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 6, 8);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 6, 8, 9, 0);

    @Test
    @DisplayName("스냅샷을 환율 마스터 엔티티로 변환한다.")
    void toExchangeRate_success() {
        // given
        ExchangeRateSnapshot snapshot = createOfficialSnapshot();

        // when
        ExchangeRate exchangeRate = snapshot.toExchangeRate();

        // then
        assertThat(exchangeRate.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(exchangeRate.getTargetCurrency()).isEqualTo(Currency.KRW);
        assertThat(exchangeRate.getRate()).isEqualByComparingTo("1350.00000000");
        assertThat(exchangeRate.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(exchangeRate.getSource()).isEqualTo(ExchangeRateSource.API);
    }

    @Test
    @DisplayName("공식 환율 스냅샷을 공식 환율 이력으로 변환한다.")
    void toOfficialHistory_success() {
        // given
        ExchangeRateSnapshot snapshot = createOfficialSnapshot();

        // when
        ExchangeRateHistory history = snapshot.toOfficialHistory();

        // then
        assertThat(history.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(history.getTargetCurrency()).isEqualTo(Currency.KRW);
        assertThat(history.getRate()).isEqualByComparingTo("1350.00000000");
        assertThat(history.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(history.getSource()).isEqualTo(ExchangeRateSource.API);
        assertThat(history.getSourceHistoryId()).isNull();
    }

    @Test
    @DisplayName("가환율 스냅샷은 공식 환율 이력으로 변환할 수 없다.")
    void provisionalSnapshotToOfficialHistory_throwsException() {
        // given
        ExchangeRateSnapshot snapshot = new ExchangeRateSnapshot(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                RATE_DATE,
                1,
                ExchangeRateType.PROVISIONAL,
                ExchangeRateSource.DB_FALLBACK,
                FETCHED_AT
        );

        // when & then
        assertThatThrownBy(snapshot::toOfficialHistory)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("공식 환율");
    }

    private ExchangeRateSnapshot createOfficialSnapshot() {
        return new ExchangeRateSnapshot(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.00000000"),
                RATE_DATE,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                FETCHED_AT
        );
    }
}
