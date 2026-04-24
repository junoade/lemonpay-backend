package com.lemonpay.wallet.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.ledger.domain.Direction;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceService;
import com.lemonpay.wallet.domain.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChargeUseCaseTest {

    @InjectMocks
    private ChargeUseCase chargeUsecase;

    @Mock
    private WalletService walletService;
    @Mock
    private WalletBalanceService walletBalanceService;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @DisplayName("지갑 충전 성공 케이스")
    @Nested
    class Success {

        @Test
        @DisplayName("지갑에 KRW 충전시 잔액이 증가되고, 원장에 정상적으로 기록된다.")
        void chargeKrw_withActiveWallet() {
            // given
            UUID walletId = UUID.randomUUID();
            Money chargeAmount = Money.won(2000);

            Wallet wallet = mock(Wallet.class);
            WalletBalance walletBalance = WalletBalance.zero(wallet, Currency.KRW);

            willDoNothing().given(wallet).validateChargeable();
            given(walletService.getWallet(walletId)).willReturn(wallet);
            given(walletBalanceService.getWalletBalance(walletId, Currency.KRW)).willReturn(walletBalance);

            // when
            chargeUsecase.charge(walletId, chargeAmount);

            // then
            then(wallet).should().validateChargeable();
            assertThat(walletBalance.getBalance()).isEqualByComparingTo(chargeAmount.amount());

            assertLedgerEntry(chargeAmount, walletBalance);
        }

        @Test
        @DisplayName("FROZEN 상태인 지갑도 KRW 충전이 가능하며, 원장에 정상적으로 기록된다.")
        void chargeKrw_withFrozenWallet() {
            // given
            UUID walletId = UUID.randomUUID();
            Money chargeAmount = Money.won(2000);

            Wallet wallet = mock(Wallet.class);
            WalletBalance walletBalance = WalletBalance.zero(wallet, Currency.KRW);
            given(walletService.getWallet(walletId)).willReturn(wallet);
            given(walletBalanceService.getWalletBalance(walletId, Currency.KRW)).willReturn(walletBalance);

            // when
            wallet.freeze();
            chargeUsecase.charge(walletId, chargeAmount);

            // then
            then(wallet).should().validateChargeable();
            assertThat(walletBalance.getBalance()).isEqualByComparingTo(chargeAmount.amount());

            assertLedgerEntry(chargeAmount, walletBalance);
        }

        private void assertLedgerEntry(Money chargeAmount, WalletBalance walletBalance) {
            ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
            then(ledgerEntryRepository).should().save(captor.capture());
            LedgerEntry saved = captor.getValue();
            assertThat(saved.getAmount()).isEqualByComparingTo(chargeAmount.amount()); // 충전금액
            assertThat(saved.getBalanceAfter()).isEqualByComparingTo(walletBalance.getBalance()); // 누적잔액
            assertThat(saved.getEntryType()).isEqualTo(EntryType.CHARGE);
            assertThat(saved.getDirection()).isEqualTo(Direction.CREDIT);
            assertThat(saved.getCurrency()).isEqualTo(Currency.KRW);
        }
    }

    @DisplayName("지갑 충전 실패 케이스")
    @Nested
    class Failure {

        @Test
        @DisplayName("지갑 상태가 입금 불가능한 상태에서 충전하려는 경우 예외가 발생하며 실패한다.")
        void chargeKrw_withInvalidWallet() {
            // given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = mock(Wallet.class);

            Money chargeAmount = Money.won(2000);
            WalletBalance walletBalance = WalletBalance.zero(wallet, Currency.KRW);

            willThrow(IllegalStateException.class).given(wallet).validateChargeable();
            given(walletService.getWallet(walletId)).willReturn(wallet);
            // given(walletBalanceService.getWalletBalance(walletId, Currency.KRW)).willReturn(walletBalance);

            // when & then
            assertThatThrownBy(() -> chargeUsecase.charge(walletId, chargeAmount))
                    .isInstanceOf(IllegalStateException.class);
            then(ledgerEntryRepository).should(never()).save(any());

        }

        @Test
        @DisplayName("KRW 충전 금액이 최소 충전 금액보다 미만인 경우 예외가 발생하며 실패한다.")
        void chargeKrw_withLessThanMinimumAmount() {
            // given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = mock(Wallet.class);

            Money chargeAmount = Money.won(100);
            WalletBalance walletBalance = WalletBalance.zero(wallet, Currency.KRW);

            given(walletService.getWallet(walletId)).willReturn(wallet);
            // given(walletBalanceService.getWalletBalance(walletId, Currency.KRW)).willReturn(walletBalance);

            assertThatThrownBy(() -> chargeUsecase.charge(walletId, chargeAmount))
                    .isInstanceOf(CoreException.class);

            then(ledgerEntryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("KRW 충전 금액이 최대 충전 금액보다 초과된 경우 예외가 발생하며 실패한다.")
        void chargeKrw_withBiggerThanMaximumAmount() {
            // given
            UUID walletId = UUID.randomUUID();
            Wallet wallet = mock(Wallet.class);

            Money chargeAmount = Money.won(1_000_001);
            WalletBalance walletBalance = WalletBalance.zero(wallet, Currency.KRW);

            given(walletService.getWallet(walletId)).willReturn(wallet);
            // given(walletBalanceService.getWalletBalance(walletId, Currency.KRW)).willReturn(walletBalance);

            assertThatThrownBy(() -> chargeUsecase.charge(walletId, chargeAmount))
                    .isInstanceOf(CoreException.class);

            then(ledgerEntryRepository).should(never()).save(any());
        }
    }
}