package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeQuoteTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 9, 28);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 9, 28, 9, 0);

    @Nested
    @DisplayName("환율 통화쌍 방향")
    class CurrencyPairDirection {

        @Test
        @DisplayName("USD/KRW 환율은 USD 기준 금액을 KRW 목표 금액으로 환산한다.")
        void convertsUsdToKrw() {
            // given
            ExchangeRate usdKrwRate = createRate(Currency.USD, Currency.KRW, "1350.00000000");

            // when
            ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.usd("100.00"), usdKrwRate);

            // then
            assertThat(quote.baseMoney()).isEqualTo(Money.usd("100.00"));
            assertThat(quote.targetMoney()).isEqualTo(Money.won(135_000));
            assertThat(quote.appliedRate()).isEqualByComparingTo("1350.00000000");
        }

        @Test
        @DisplayName("KRW/USD 환율은 KRW 기준 금액을 USD 목표 금액으로 환산한다.")
        void convertsKrwToUsd() {
            // given
            ExchangeRate krwUsdRate = createRate(Currency.KRW, Currency.USD, "0.00074074");

            // when
            ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.won(135_000), krwUsdRate);

            // then
            assertThat(quote.baseMoney()).isEqualTo(Money.won(135_000));
            assertThat(quote.targetMoney()).isEqualTo(Money.usd("100.00"));
            assertThat(quote.appliedRate()).isEqualByComparingTo("0.00074074");
        }
    }

    @Nested
    @DisplayName("기준 금액 환전 견적")
    class FromBaseAmount {

        @Test
        @DisplayName("기준 통화 금액과 환율로 목표 통화 금액을 계산한다.")
        void calculatesTargetAmount() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "1350.00000000");
            Money baseMoney = Money.usd("100.00");

            // when
            ExchangeQuote quote = ExchangeQuote.fromBaseAmount(baseMoney, rate);

            // then
            assertThat(quote.baseMoney()).isEqualTo(Money.usd("100.00"));
            assertThat(quote.targetMoney()).isEqualTo(Money.won(135_000));
            assertThat(quote.appliedRate()).isEqualByComparingTo("1350.00000000");
        }

        @Test
        @DisplayName("목표 통화의 소수 자릿수에 맞춰 환산 금액을 반올림한다.")
        void roundsTargetAmount() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "1350.50000000");

            // when
            ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.usd("1.00"), rate);

            // then
            assertThat(quote.targetMoney()).isEqualTo(Money.won(1_351));
        }

        @Test
        @DisplayName("입력 금액의 통화가 환율의 기준 통화와 다르면 견적을 생성할 수 없다.")
        void rejectsDifferentBaseCurrency() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "1350.00000000");

            // when & then
            assertThatThrownBy(() -> ExchangeQuote.fromBaseAmount(Money.won(100_000), rate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("기준 통화");
        }
    }

    @Nested
    @DisplayName("목표 금액 환전 견적")
    class ForTargetAmount {

        @Test
        @DisplayName("목표 통화 금액과 환율로 필요한 기준 통화 금액을 역산한다.")
        void calculatesBaseAmount() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "1350.00000000");
            Money targetMoney = Money.won(135_000);

            // when
            ExchangeQuote quote = ExchangeQuote.forTargetAmount(targetMoney, rate);

            // then
            assertThat(quote.baseMoney()).isEqualTo(Money.usd("100.00"));
            assertThat(quote.targetMoney()).isEqualTo(Money.won(135_000));
            assertThat(quote.appliedRate()).isEqualByComparingTo("1350.00000000");
        }

        @Test
        @DisplayName("역산 금액은 기준 통화의 소수 자릿수에 맞춰 반올림한다.")
        void roundsBaseAmount() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "3.00000000");

            // when
            ExchangeQuote quote = ExchangeQuote.forTargetAmount(Money.won(10), rate);

            // then
            assertThat(quote.baseMoney()).isEqualTo(Money.usd("3.33"));
        }

        @Test
        @DisplayName("입력 금액의 통화가 환율의 목표 통화와 다르면 견적을 생성할 수 없다.")
        void rejectsDifferentTargetCurrency() {
            // given
            ExchangeRate rate = createRate(Currency.USD, Currency.KRW, "1350.00000000");

            // when & then
            assertThatThrownBy(() -> ExchangeQuote.forTargetAmount(Money.usd("100.00"), rate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("대상 통화");
        }
    }

    private ExchangeRate createRate(Currency base, Currency target, String rate) {
        return ExchangeRate.create(
                base,
                target,
                new BigDecimal(rate),
                RATE_DATE,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                FETCHED_AT
        );
    }
}
