package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ExchangeTransactionTest {

    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final String KEY = "exchange-request-001";
    private static final LocalDate RATE_DATE = LocalDate.of(2026, 10, 10);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 10, 10, 10, 0);

    @Test
    @DisplayName("KRW/USD 환전 거래는 견적 금액과 소수점 8자리 적용 환율 및 고시 정보를 보존한다.")
    void recordsQuote() {
        // given: 정방향 KRW → USD 환율을 사용
        ExchangeRate rate = rate(Currency.KRW, Currency.USD, "0.00074074");
        ExchangeQuote quote = ExchangeQuote.fromBaseAmount(
                Money.won(135_000), rate);

        // when
        ExchangeTransaction transaction = ExchangeTransaction.completed(
                WALLET_ID, KEY, quote, rate,
                Money.won(65_000), Money.usd("125.00")
        );

        // then
        assertThat(transaction.getWalletId()).isEqualTo(WALLET_ID);
        assertThat(transaction.getIdempotencyKey()).isEqualTo(KEY);
        assertThat(transaction.getBaseCurrency()).isEqualTo(Currency.KRW);
        assertThat(transaction.getBaseAmount()).isEqualByComparingTo("135000");
        assertThat(transaction.getTargetCurrency()).isEqualTo(Currency.USD);
        assertThat(transaction.getTargetAmount()).isEqualByComparingTo("100.00");
        assertThat(transaction.getAppliedRate()).isEqualByComparingTo("0.00074074");
        assertThat(transaction.getRateDate()).isEqualTo(RATE_DATE);
        assertThat(transaction.getRoundNo()).isEqualTo(2);
        assertThat(transaction.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(transaction.getRateSource()).isEqualTo(ExchangeRateSource.API);
    }

    @Test
    @DisplayName("USD/KRW 환전 거래는 환전 금액과 구분하여 두 통화의 변경 후 잔액을 보존한다.")
    void recordsBalances() {
        // given
        ExchangeRate rate = rate(Currency.USD, Currency.KRW, "1350.00000000");
        ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.usd("100.00"), rate);
        Money baseAfter = Money.usd("75.25");
        Money targetAfter = Money.won(145_000);

        // when
        ExchangeTransaction transaction = ExchangeTransaction.completed(
                WALLET_ID, KEY, quote, rate, baseAfter, targetAfter
        );

        // then
        assertThat(transaction.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(transaction.getTargetCurrency()).isEqualTo(Currency.KRW);
        assertThat(transaction.getBaseAmount()).isEqualByComparingTo("100.00");
        assertThat(transaction.getTargetAmount()).isEqualByComparingTo("135000");
        assertThat(transaction.getBaseBalanceAfter()).isEqualByComparingTo("75.25");
        assertThat(transaction.getTargetBalanceAfter()).isEqualByComparingTo("145000");
    }


    private ExchangeRate rate(Currency base, Currency target, String value) {
        return ExchangeRate.create(
                base, target, new BigDecimal(value), RATE_DATE, 2,
                ExchangeRateType.OFFICIAL, ExchangeRateSource.API, FETCHED_AT
        );
    }

}