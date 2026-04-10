package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletBalanceTest {

    @Nested
    @DisplayName("지갑 잔액 증가 태스트")
    class IncreaseBalance {
        Wallet wallet;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
        }

        @Test
        @DisplayName("충전 통화가 동일한 지갑이고, 그 금액 양수일 때 정상적으로 지갑 잔액이 증가한다")
        void withSameCurrency_thenIncreaseBalance() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);
            BigDecimal beforeBalance = walletBalance.getBalance();

            walletBalance.increase(Money.won(2000));
            BigDecimal afterBalance = walletBalance.getBalance();

            assertThat(afterBalance).isEqualByComparingTo(beforeBalance.add(BigDecimal.valueOf(2000)));
            assertThat(walletBalance.toMoney()).isEqualTo(Money.of(afterBalance, Currency.KRW));
        }

        @Test
        @DisplayName("충전 통화가 동일한 지갑이지만, 그 금액이 음수인 경우 예외가 발생한다.")
        void withSameCurrencyButNegative_throwException() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            assertThrows(IllegalArgumentException.class, () -> walletBalance.increase(Money.won(-2000)));
        }

        @Test
        @DisplayName("충전 통화가 다른 지갑에 충전하려고 하면, 통화 불일치 예외가 발생한다.")
        void withDifferentCurrency_throwException() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            assertThrows(IllegalArgumentException.class, () -> walletBalance.increase(Money.jpy(2000)));
        }

    }

    @Nested
    @DisplayName("지갑 잔액 감소 테스트")
    class DecreaseBalance {

        Wallet wallet;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
            wallet.initDefaultCurrency();
            wallet.getBalance(Currency.KRW).increase(Money.won(2000));
        }

        @Test
        @DisplayName("출금 통화가 동일하고, 그 금액 양수일 때 정상적으로 지갑 잔액이 감소한다")
        void withSameCurrency_thenDecreaseBalance() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            BigDecimal beforeBalance = walletBalance.getBalance();
            walletBalance.decrease(Money.won(1000));
            BigDecimal afterBalance = walletBalance.getBalance();

            assertThat(afterBalance).isEqualByComparingTo(beforeBalance.subtract(BigDecimal.valueOf(1000)));
            assertThat(walletBalance.toMoney()).isEqualTo(Money.of(afterBalance, Currency.KRW));
        }

        @Test
        @DisplayName("출금 통화가 동일하고, 현재 잔액만큼 출금할 때 잔액은 0으로 된다")
        void withSameCurrencySameBalance_thenZeroBalance() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            BigDecimal beforeBalance = walletBalance.getBalance();
            walletBalance.decrease(Money.won(2000));
            BigDecimal afterBalance = walletBalance.getBalance();

            assertThat(afterBalance).isEqualByComparingTo(beforeBalance.subtract(BigDecimal.valueOf(2000)));
            assertThat(walletBalance.toMoney()).isEqualTo(Money.of(afterBalance, Currency.KRW));

        }

        @Test
        @DisplayName("출금 통화가 동일하지만, 잔액이 부족한 경우 예외가 발생한다.")
        void withSameCurrencyNotEnoughBalance_throwException() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            assertThrows(IllegalArgumentException.class, () -> walletBalance.decrease(Money.won(3000)));
        }

        @Test
        @DisplayName("출금 통화가 다른데, 출금하려고 하면 예외가 발생한다.")
        void withDifferentCurrency_throwException() {
            WalletBalance walletBalance = wallet.getBalance(Currency.KRW);

            assertThrows(IllegalArgumentException.class, () -> walletBalance.decrease(Money.jpy(2000)));
        }
    }


    private Member createTestMember() {
        return Member.create("test@gmail.com", "JunhoTest", "+821012341234");
    }

    private Wallet createTestWallet() {
        Member member = createTestMember();
        return Wallet.create(member, "TestWallet", "BASC-V01");
    }
}