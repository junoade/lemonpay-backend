package com.lemonpay.ledger.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.member.domain.Member;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LedgerEntryTest {


    @Nested
    @DisplayName("잔액 충전 시 원장 생성 테스트")
    class OnCharge {
        Wallet wallet;
        WalletBalance krwBalance;
        WalletBalance usdBalance;
        WalletBalance jpyBalance;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
            krwBalance = wallet.getBalance(Currency.KRW);
            usdBalance = wallet.getBalance(Currency.USD);
            jpyBalance = wallet.getBalance(Currency.JPY);
        }


        @Test
        @DisplayName("주어진 원화 충전 금액이 KRW 원장으로 기록되고, 거래 후 잔액이 증가된 값과 일치한다")
        void chargeKrw_thenLedgerRecordedCredit() {
            Money charge = Money.won(200_000);

            krwBalance.increase(charge);
            LedgerEntry ledgerEntry = LedgerEntry.of(
                    wallet.getId(),
                    charge,
                    Money.of(krwBalance.getBalance(), Currency.KRW),
                    EntryType.CHARGE
            );

            assertAll(
                    () -> assertThat(ledgerEntry).isNotNull(),
                    () -> assertThat(ledgerEntry.getEntryType()).isEqualTo(EntryType.CHARGE),
                    () -> assertThat(ledgerEntry.getDirection()).isEqualTo(Direction.CREDIT),
                    () -> assertThat(ledgerEntry.getCurrency()).isEqualTo(Currency.KRW),
                    () -> assertThat(ledgerEntry.getAmount()).isEqualByComparingTo(charge.amount()),
                    () -> assertThat(ledgerEntry.getBalanceAfter()).isEqualByComparingTo(krwBalance.getBalance())
            );

        }

        @Test
        @DisplayName("주어진 달러 충전 금액이 USD 원장으로 기록되고, 거래 후 잔액이 증가된 값과 일치한다")
        void chargeUsd_thenLedgerRecordedCredit() {
            Money charge = Money.usd("100");

            usdBalance.increase(charge);
            LedgerEntry ledgerEntry = LedgerEntry.of(
                    wallet.getId(),
                    charge,
                    Money.of(usdBalance.getBalance(), Currency.USD),
                    EntryType.CHARGE
            );

            assertAll(
                    () -> assertThat(ledgerEntry).isNotNull(),
                    () -> assertThat(ledgerEntry.getEntryType()).isEqualTo(EntryType.CHARGE),
                    () -> assertThat(ledgerEntry.getDirection()).isEqualTo(Direction.CREDIT),
                    () -> assertThat(ledgerEntry.getCurrency()).isEqualTo(Currency.USD),
                    () -> assertThat(ledgerEntry.getAmount()).isEqualByComparingTo(charge.amount()),
                    () -> assertThat(ledgerEntry.getBalanceAfter()).isEqualByComparingTo(usdBalance.getBalance())
            );

        }

        @Test
        @DisplayName("주어진 엔화 충전 금액이 JPY 원장으로 기록되고, 거래 후 잔액이 증가된 값과 일치한다")
        void chargeJpy_thenLedgerRecordedCredit() {
            Money charge = Money.jpy(1000);

            jpyBalance.increase(charge);
            LedgerEntry ledgerEntry = LedgerEntry.of(
                    wallet.getId(),
                    charge,
                    Money.of(jpyBalance.getBalance(), Currency.JPY),
                    EntryType.CHARGE
            );

            assertAll(
                    () -> assertThat(ledgerEntry).isNotNull(),
                    () -> assertThat(ledgerEntry.getEntryType()).isEqualTo(EntryType.CHARGE),
                    () -> assertThat(ledgerEntry.getDirection()).isEqualTo(Direction.CREDIT),
                    () -> assertThat(ledgerEntry.getCurrency()).isEqualTo(Currency.JPY),
                    () -> assertThat(ledgerEntry.getAmount()).isEqualByComparingTo(charge.amount()),
                    () -> assertThat(ledgerEntry.getBalanceAfter()).isEqualByComparingTo(jpyBalance.getBalance())
            );

        }



       /* @Test
        @DisplayName("결제(payment) 원장 생성 테스트")
        void payment_thenLedgerRecordedDebit() {

        }

        @Test
        @DisplayName("환전 출금(FX_WITHDRAW) 원장 생성 테스트")
        void withdraw_thenLedgerRecordedDebit() {

        }

        @Test
        @DisplayName("환전 입금(FX_DEPOSIT) 원장 생성시 CREDIT으로 원장이 기록된다")
        void deposit_thenLedgerRecordedCredit() {

        }*/
    }


    @Nested
    @DisplayName("잔액 지불 시 원장 생성 테스트")
    class OnPayment {
        Wallet wallet;
        WalletBalance krwBalance;
        WalletBalance usdBalance;
        WalletBalance jpyBalance;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
            krwBalance = wallet.getBalance(Currency.KRW);
            usdBalance = wallet.getBalance(Currency.USD);
            jpyBalance = wallet.getBalance(Currency.JPY);
        }

        @DisplayName("멀티 통화 결제 시 원장이 정상 생성된다.")
        @ParameterizedTest(name = "{0} 결제 시 DEBIT 원장이 생성되고 잔액이 감소한다.")
        @MethodSource("paymentCurrencies")
        void payment_thenLedgerRecordedDebit(Currency currency, Money paymentAmount) {
            // given
            WalletBalance balance = wallet.getBalance(currency);

            // when
            balance.decrease(paymentAmount);
            LedgerEntry ledgerEntry = LedgerEntry.of(
                    wallet.getId(),
                    paymentAmount,
                    Money.of(balance.getBalance(), currency),
                    EntryType.PAYMENT
            );

            // then
            assertAll(
                    () -> assertThat(ledgerEntry.getEntryType()).isEqualTo(EntryType.PAYMENT),
                    () -> assertThat(ledgerEntry.getDirection()).isEqualTo(EntryType.PAYMENT.getDirection()),
                    () -> assertThat(ledgerEntry.getCurrency()).isEqualTo(currency),
                    () -> assertThat(ledgerEntry.getAmount()).isEqualByComparingTo(paymentAmount.amount()),
                    () -> assertThat(ledgerEntry.getBalanceAfter()).isEqualByComparingTo(balance.getBalance())
            );
        }

        static Stream<Arguments> paymentCurrencies() {
            return Stream.of(
                    Arguments.of(Currency.KRW, Money.won(100_000)),
                    Arguments.of(Currency.USD, Money.usd("100.24")),
                    Arguments.of(Currency.JPY, Money.jpy(1000))
            );
        }
    }

    private Member createTestMember() {
        return Member.create("test@gmail.com", "JunhoTest", "+821012341234");
    }

    private Wallet createTestWallet() {
        Member member = createTestMember();
        Wallet wallet = Wallet.create(member, "TestWallet", "BASC-V01");
        setTestBalance(wallet);
        return wallet;
    }

    private void setTestBalance(Wallet wallet) {
        WalletBalance usdWalletBalance = wallet.getBalance(Currency.USD);
        WalletBalance krwWalletBalance = wallet.getBalance(Currency.KRW);
        WalletBalance jpyWalletBalance = wallet.getBalance(Currency.JPY);
        usdWalletBalance.increase(Money.usd("200"));
        krwWalletBalance.increase(Money.won(100_000_000));
        jpyWalletBalance.increase(Money.jpy(500_000));
    }
}