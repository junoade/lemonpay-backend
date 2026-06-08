package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateHistoryTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 6, 8);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 6, 8, 9, 0);

    @Nested
    @DisplayName("공식 환율 이력 생성 테스트")
    class Official {

        @Test
        @DisplayName("official() 시 OFFICIAL 유형의 환율 이력이 생성된다.")
        void createOfficialHistory_success() {
            // given & when
            ExchangeRateHistory history = createOfficialUsdKrwHistory();

            // then
            assertThat(history.getBaseCurrency()).isEqualTo(Currency.USD);
            assertThat(history.getTargetCurrency()).isEqualTo(Currency.KRW);
            assertThat(history.getRate()).isEqualByComparingTo("1350.12345679");
            assertThat(history.getRate().scale()).isEqualTo(8);
            assertThat(history.getRateDate()).isEqualTo(RATE_DATE);
            assertThat(history.getRoundNo()).isEqualTo(1);
            assertThat(history.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
            assertThat(history.getSource()).isEqualTo(ExchangeRateSource.API);
            assertThat(history.getSourceHistoryId()).isNull();
            assertThat(history.getFetchedAt()).isEqualTo(FETCHED_AT);
        }
    }

    @Nested
    @DisplayName("가환율 이력 생성 테스트")
    class Provisional {

        @Test
        @DisplayName("provisional() 시 PROVISIONAL 유형과 원본 이력 ID가 저장된다.")
        void createProvisionalHistory_success() {
            // given & when
            ExchangeRateHistory history = ExchangeRateHistory.provisional(
                    Currency.USD,
                    Currency.KRW,
                    new BigDecimal("1355.5"),
                    RATE_DATE.plusDays(1),
                    1,
                    10L,
                    FETCHED_AT.plusDays(1)
            );

            // then
            assertThat(history.getBaseCurrency()).isEqualTo(Currency.USD);
            assertThat(history.getTargetCurrency()).isEqualTo(Currency.KRW);
            assertThat(history.getRate()).isEqualByComparingTo("1355.50000000");
            assertThat(history.getRateType()).isEqualTo(ExchangeRateType.PROVISIONAL);
            assertThat(history.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
            assertThat(history.getSourceHistoryId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("가환율은 원본 이력 ID가 필수다.")
        void provisionalWithoutSourceHistoryId_throwsException() {
            assertThatThrownBy(() -> ExchangeRateHistory.provisional(
                    Currency.USD,
                    Currency.KRW,
                    BigDecimal.valueOf(1355),
                    RATE_DATE.plusDays(1),
                    1,
                    null,
                    FETCHED_AT.plusDays(1)
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("원본 이력 ID");
        }
    }

    @Nested
    @DisplayName("불변조건 테스트")
    class Validation {

        @Test
        @DisplayName("기준 통화와 대상 통화가 같으면 생성할 수 없다.")
        void sameCurrencyPair_throwsException() {
            assertThatThrownBy(() -> ExchangeRateHistory.official(
                    Currency.KRW,
                    Currency.KRW,
                    BigDecimal.ONE,
                    RATE_DATE,
                    1,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("달라야");
        }

        @Test
        @DisplayName("환율은 0보다 커야 한다.")
        void zeroRate_throwsException() {
            assertThatThrownBy(() -> ExchangeRateHistory.official(
                    Currency.USD,
                    Currency.KRW,
                    BigDecimal.ZERO,
                    RATE_DATE,
                    1,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야");
        }

        @Test
        @DisplayName("환율 회차는 1 이상이어야 한다.")
        void invalidRoundNo_throwsException() {
            assertThatThrownBy(() -> ExchangeRateHistory.official(
                    Currency.USD,
                    Currency.KRW,
                    BigDecimal.valueOf(1350),
                    RATE_DATE,
                    0,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    private ExchangeRateHistory createOfficialUsdKrwHistory() {
        return ExchangeRateHistory.official(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.123456789"),
                RATE_DATE,
                1,
                ExchangeRateSource.API,
                FETCHED_AT
        );
    }
}
