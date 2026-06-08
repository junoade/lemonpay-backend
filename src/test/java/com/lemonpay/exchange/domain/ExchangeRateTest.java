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

class ExchangeRateTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 6, 8);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 6, 8, 9, 0);

    @Nested
    @DisplayName("생성 테스트")
    class Create {

        @Test
        @DisplayName("create() 시 통화쌍, 환율, 기준일, 회차, 유형, 출처가 저장된다.")
        void createExchangeRate_success() {
            // given & when
            ExchangeRate exchangeRate = createUsdKrwRate();

            // then
            assertThat(exchangeRate.getBaseCurrency()).isEqualTo(Currency.USD);
            assertThat(exchangeRate.getTargetCurrency()).isEqualTo(Currency.KRW);
            assertThat(exchangeRate.getRate()).isEqualByComparingTo("1350.12345679");
            assertThat(exchangeRate.getRate().scale()).isEqualTo(8);
            assertThat(exchangeRate.getRateDate()).isEqualTo(RATE_DATE);
            assertThat(exchangeRate.getRoundNo()).isEqualTo(1);
            assertThat(exchangeRate.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
            assertThat(exchangeRate.getSource()).isEqualTo(ExchangeRateSource.API);
            assertThat(exchangeRate.getFetchedAt()).isEqualTo(FETCHED_AT);
        }

        @Test
        @DisplayName("기준 통화와 대상 통화가 같으면 생성할 수 없다.")
        void sameCurrencyPair_throwsException() {
            assertThatThrownBy(() -> ExchangeRate.create(
                    Currency.KRW,
                    Currency.KRW,
                    BigDecimal.valueOf(1),
                    RATE_DATE,
                    1,
                    ExchangeRateType.OFFICIAL,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("달라야");
        }

        @Test
        @DisplayName("환율은 0보다 커야 한다.")
        void zeroRate_throwsException() {
            assertThatThrownBy(() -> ExchangeRate.create(
                    Currency.USD,
                    Currency.KRW,
                    BigDecimal.ZERO,
                    RATE_DATE,
                    1,
                    ExchangeRateType.OFFICIAL,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야");
        }

        @Test
        @DisplayName("환율 회차는 1 이상이어야 한다.")
        void invalidRoundNo_throwsException() {
            assertThatThrownBy(() -> ExchangeRate.create(
                    Currency.USD,
                    Currency.KRW,
                    BigDecimal.valueOf(1350),
                    RATE_DATE,
                    0,
                    ExchangeRateType.OFFICIAL,
                    ExchangeRateSource.API,
                    FETCHED_AT
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1 이상");
        }
    }

    @Nested
    @DisplayName("갱신 테스트")
    class Update {

        @Test
        @DisplayName("update() 시 현재 적용 환율 정보가 갱신된다.")
        void updateExchangeRate_success() {
            // given
            ExchangeRate exchangeRate = createUsdKrwRate();
            LocalDate nextRateDate = RATE_DATE.plusDays(1);
            LocalDateTime nextFetchedAt = FETCHED_AT.plusDays(1);

            // when
            exchangeRate.update(
                    new BigDecimal("1360.1"),
                    nextRateDate,
                    2,
                    ExchangeRateType.PROVISIONAL,
                    ExchangeRateSource.DB_FALLBACK,
                    nextFetchedAt
            );

            // then
            assertThat(exchangeRate.getBaseCurrency()).isEqualTo(Currency.USD);
            assertThat(exchangeRate.getTargetCurrency()).isEqualTo(Currency.KRW);
            assertThat(exchangeRate.getRate()).isEqualByComparingTo("1360.10000000");
            assertThat(exchangeRate.getRate().scale()).isEqualTo(8);
            assertThat(exchangeRate.getRateDate()).isEqualTo(nextRateDate);
            assertThat(exchangeRate.getRoundNo()).isEqualTo(2);
            assertThat(exchangeRate.getRateType()).isEqualTo(ExchangeRateType.PROVISIONAL);
            assertThat(exchangeRate.getSource()).isEqualTo(ExchangeRateSource.DB_FALLBACK);
            assertThat(exchangeRate.getFetchedAt()).isEqualTo(nextFetchedAt);
        }
    }

    private ExchangeRate createUsdKrwRate() {
        return ExchangeRate.create(
                Currency.USD,
                Currency.KRW,
                new BigDecimal("1350.123456789"),
                RATE_DATE,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                FETCHED_AT
        );
    }
}
