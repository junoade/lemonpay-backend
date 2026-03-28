package com.lemonpay.common.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class MoneyTest {


    @Nested
    @DisplayName("생성")
    class Create {
        @Test
        @DisplayName("KRW 정상 테스트 생성")
        void createWon() {
            Money money = Money.won(100_000);
            assertThat(money.amount()).isEqualByComparingTo("100000");
            assertThat(money.currency()).isEqualTo(Currency.KRW);
        }

        @Test
        @DisplayName("USD 소수점 2자리 적용")
        void createUsdWithScale() {
            Money money = Money.usd("9.999");

            // 9.999 → HALF_UP → 10.00
            assertThat(money.amount()).isEqualByComparingTo("10.00");
            assertThat(money.amount().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("KRW에 소수점 입력 시 0자리로 반올림")
        void krwScaleEnforced() {
            Money money = Money.of(new BigDecimal("1000.6"), Currency.KRW);

            assertThat(money.amount()).isEqualByComparingTo("1001");
            assertThat(money.amount().scale()).isEqualTo(0);
        }

        @Test
        @DisplayName("JPY 0자리 소수점 적용")
        void jpyScaleEnforced() {
            Money money = Money.jpy(500);

            assertThat(money.amount()).isEqualByComparingTo("500");
            assertThat(money.amount().scale()).isEqualTo(0);
        }

        @Test
        @DisplayName("0원 생성 가능")
        void createZero() {
            Money money = Money.zero(Currency.KRW);

            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("음수 금액 생성 시 예외")
        void negativeAmountThrows() {
            assertThatThrownBy(() -> Money.won(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
        }

        @Test
        @DisplayName("null 금액 생성 시 예외")
        void nullAmountThrows() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null, Currency.KRW))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 통화 생성 시 예외")
        void nullCurrencyThrows() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("연산")
    class Arithmetic {

        @Test
        @DisplayName("같은 통화 덧셈")
        void addSameCurrency() {
            Money a = Money.won(1_000);
            Money b = Money.won(2_000);

            Money result = a.add(b);

            assertThat(result).isEqualTo(Money.won(3_000));
        }

        @Test
        @DisplayName("같은 통화 뺄셈")
        void subtractSameCurrency() {
            Money a = Money.won(5_000);
            Money b = Money.won(2_000);

            Money result = a.subtract(b);

            assertThat(result).isEqualTo(Money.won(3_000));
        }

        @Test
        @DisplayName("뺄셈 결과 음수 시 예외")
        void subtractResultNegativeThrows() {
            Money a = Money.won(1_000);
            Money b = Money.won(2_000);

            assertThatThrownBy(() -> a.subtract(b))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("음수");
        }

        @Test
        @DisplayName("다른 통화 덧셈 시 예외")
        void addDifferentCurrencyThrows() {
            Money krw = Money.won(1_000);
            Money usd = Money.usd("10.00");

            assertThatThrownBy(() -> krw.add(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("통화 불일치");
        }

        @Test
        @DisplayName("다른 통화 뺄셈 시 예외")
        void subtractDifferentCurrencyThrows() {
            Money krw = Money.won(1_000);
            Money jpy = Money.jpy(100);

            assertThatThrownBy(() -> krw.subtract(jpy))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("통화 불일치");
        }

        @Test
        @DisplayName("USD 덧셈 — 소수점 유지")
        void addUsdKeepsScale() {
            Money a = Money.usd("10.50");
            Money b = Money.usd("3.25");

            Money result = a.add(b);

            assertThat(result).isEqualTo(Money.usd("13.75"));
            assertThat(result.amount().scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("비교")
    class Comparison {

        @Test
        @DisplayName("큰 금액 >= 작은 금액")
        void greaterThanOrEqual() {
            Money a = Money.won(5_000);
            Money b = Money.won(3_000);

            assertThat(a.isGreaterThanOrEqual(b)).isTrue();
        }

        @Test
        @DisplayName("같은 금액 >= 같은 금액")
        void equalAmountIsGreaterThanOrEqual() {
            Money a = Money.won(5_000);
            Money b = Money.won(5_000);

            assertThat(a.isGreaterThanOrEqual(b)).isTrue();
        }

        @Test
        @DisplayName("작은 금액 >= 큰 금액 → false")
        void lessThanIsNotGreaterThanOrEqual() {
            Money a = Money.won(1_000);
            Money b = Money.won(5_000);

            assertThat(a.isGreaterThanOrEqual(b)).isFalse();
        }

        @Test
        @DisplayName("다른 통화 비교 시 예외")
        void compareDifferentCurrencyThrows() {
            Money krw = Money.won(1_000);
            Money usd = Money.usd("1.00");

            assertThatThrownBy(() -> krw.isGreaterThanOrEqual(usd))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("동등성")
    class Equality {

        @Test
        @DisplayName("같은 금액 + 같은 통화 → 동등")
        void sameAmountAndCurrencyAreEqual() {
            Money a = Money.won(1_000);
            Money b = Money.won(1_000);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("다른 금액 → 동등하지 않음")
        void differentAmountNotEqual() {
            assertThat(Money.won(1_000)).isNotEqualTo(Money.won(2_000));
        }

        @Test
        @DisplayName("다른 통화 → 동등하지 않음")
        void differentCurrencyNotEqual() {
            assertThat(Money.won(100)).isNotEqualTo(Money.jpy(100));
        }

        @Test
        @DisplayName("BigDecimal scale이 달라도 값이 같으면 동등")
        void differentScaleSameValueAreEqual() {
            Money a = Money.of(new BigDecimal("10.00"), Currency.USD);
            Money b = Money.of(new BigDecimal("10"), Currency.USD);

            // 둘 다 scale=2로 정규화됨
            assertThat(a).isEqualTo(b);
        }
    }

    @Nested
    @DisplayName("불변성")
    class Immutability {

        @Test
        @DisplayName("연산 후 원본 변경 없음")
        void operationsDoNotMutateOriginal() {
            Money original = Money.won(5_000);
            Money other = Money.won(1_000);

            original.add(other);

            assertThat(original).isEqualTo(Money.won(5_000));
        }
    }

}