package com.lemonpay.exchange.application;

import com.lemonpay.common.auth.UserContext;
import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import com.lemonpay.exchange.domain.ExchangeTransaction;
import com.lemonpay.exchange.domain.ExchangeTransactionRepository;
import com.lemonpay.ledger.domain.Direction;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceService;
import com.lemonpay.wallet.domain.WalletService;
import com.lemonpay.wallet.domain.WalletStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeUseCaseTest {

    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String KEY = "exchange-request-001";
    private static final LocalDate RATE_DATE = LocalDate.of(2026, 10, 10);

    @Mock
    private WalletService walletService;
    @Mock
    private WalletBalanceService walletBalanceService;
    @Mock
    private ExchangeRateQueryService exchangeRateQueryService;
    @Mock
    private ExchangeTransactionRepository exchangeTransactionRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private ExchangeUseCase useCase;

    @AfterEach
    void clearContext() {
        UserContextHolder.clear();
    }

    @Nested
    @DisplayName("환전 성공")
    class HappyPath {

        @Test
        @DisplayName("USD 100을 KRW로 환전하면 두 잔액, 환전 거래 1건, 회계 원장 2건과 응답이 일치하고 통화코드 오름차순인 KRW 부터 락을 요청한다.")
        void exchangesUsdToKrw() {
            // given
            Wallet wallet = activeWallet();
            stubRate(Currency.USD, Currency.KRW, "1350.00000000");
            WalletBalance base = stubLockedBalance(wallet, Money.usd("200.00"));
            WalletBalance target = stubLockedBalance(wallet, Money.won(10_000));

            // when
            ExchangeResult result = useCase.exchange(command(Currency.USD, Currency.KRW, "100.00"));

            // then
            assertExchange(result, base, target,
                    Money.usd("100.00"), Money.won(135_000),
                    Money.usd("100.00"), Money.won(145_000), "1350.00000000");
            assertLockOrder(Currency.USD, Currency.KRW);
        }

        @Test
        @DisplayName("KRW 100,000을 USD로 환전하면 74.07 USD를 지급하고 소수점 8자리 환율을 보존하며 오름차순인 KRW부터 락을 요청한다.")
        void exchangesKrwToUsd() {
            // given
            Wallet wallet = activeWallet();
            stubRate(Currency.KRW, Currency.USD, "0.00074074");
            WalletBalance base = stubLockedBalance(wallet, Money.won(200_000));
            WalletBalance target = stubLockedBalance(wallet, Money.usd("10.00"));

            // when
            ExchangeResult result = useCase.exchange(command(Currency.KRW, Currency.USD, "100000"));

            // then
            assertExchange(result, base, target,
                    Money.won(100_000), Money.usd("74.07"),
                    Money.won(100_000), Money.usd("84.07"), "0.00074074");
            assertLockOrder(Currency.KRW, Currency.USD);
        }

        @Test
        @DisplayName("USD 잔액과 요청 금액이 같으면 전액 환전되고 출금 후 잔액은 0이다.")
        void exchangeFullBalance() {
            Wallet wallet = activeWallet();
            stubRate(Currency.USD, Currency.KRW, "1350.00000000");
            WalletBalance base = stubLockedBalance(wallet, Money.usd("100.00"));
            WalletBalance target = stubLockedBalance(wallet, Money.won(10_000));

            ExchangeResult result = useCase.exchange(
                    command(Currency.USD, Currency.KRW, "100.00"));

            assertExchange(result, base, target,
                    Money.usd("100.00"), Money.won(135_000),
                    Money.usd("0.00"), Money.won(145_000), "1350.00000000");
        }
    }

    @Nested
    @DisplayName("환전 실패")
    class UnHappyPath {

        @ParameterizedTest
        @MethodSource("missingInputs")
        @DisplayName("요청 또는 필수값이 없으면 지갑·환율·잔액 조회와 저장 전에 거부한다.")
        void rejectsMissingInput(ExchangeCommand command) {
            assertInvalidRequest(command);
            verifyNoInteractions(walletService, exchangeRateQueryService, walletBalanceService,
                    exchangeTransactionRepository, ledgerEntryRepository);
        }

        private static Stream<ExchangeCommand> missingInputs() {
            return Stream.of(
                    (ExchangeCommand) null,
                    new ExchangeCommand(null, Currency.USD, Currency.KRW, BigDecimal.ONE, KEY),
                    new ExchangeCommand(WALLET_ID, null, Currency.KRW, BigDecimal.ONE, KEY),
                    new ExchangeCommand(WALLET_ID, Currency.USD, null, BigDecimal.ONE, KEY),
                    new ExchangeCommand(WALLET_ID, Currency.USD, Currency.KRW, null, KEY)
            );
        }

        @Test
        @DisplayName("동일 통화 환전은 지갑·환율·잔액 조회와 저장 전에 거부한다.")
        void rejectsSameCurrency() {
            assertInvalidRequest(command(Currency.USD, Currency.USD, "100"));
            verifyNoInteractions(walletService, exchangeRateQueryService, walletBalanceService,
                    exchangeTransactionRepository, ledgerEntryRepository);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1"})
        @DisplayName("환전 금액이 0 또는 음수이면 지갑·환율·잔액 조회와 저장 전에 거부한다.")
        void rejectsInvalidAmount(String amount) {
            assertInvalidRequest(command(Currency.USD, Currency.KRW, amount));
            verifyNoInteractions(walletService, exchangeRateQueryService, walletBalanceService,
                    exchangeTransactionRepository, ledgerEntryRepository);
        }

        @Test
        @DisplayName("양수인 USD 요청 금액도 통화 단위 정규화 후 0이 되면 락 조회와 저장 전에 거부한다.")
        void rejectsZeroBase() {
            activeWallet();
            stubRate(Currency.USD, Currency.KRW, "1350.00000000");

            assertInvalidRequest(command(Currency.USD, Currency.KRW, "0.004"));
            verifyNoInteractions(walletBalanceService, exchangeTransactionRepository, ledgerEntryRepository);
        }

        @Test
        @DisplayName("KRW를 USD로 환산한 금액이 반올림 후 0이면 락 조회와 저장 전에 거부한다.")
        void rejectsZeroTarget() {
            activeWallet();
            stubRate(Currency.KRW, Currency.USD, "0.00074074");

            assertInvalidRequest(command(Currency.KRW, Currency.USD, "1"));
            verifyNoInteractions(walletBalanceService, exchangeTransactionRepository, ledgerEntryRepository);
        }

        @Test
        @DisplayName("타인 지갑 접근 예외를 전파하고 지갑 상세·환율·잔액 조회와 저장을 수행하지 않는다.")
        void rejectsForeignWallet() {
            UserContextHolder.set(new UserContext(USER_ID));
            CoreException failure = new CoreException(ErrorType.FORBIDDEN);
            doThrow(failure).when(walletService).validateWalletAccess(WALLET_ID, USER_ID);

            assertThatThrownBy(() -> useCase.exchange(command(Currency.USD, Currency.KRW, "100")))
                    .isSameAs(failure);
            verify(walletService, never()).getWallet(any());
            verifyNoInteractions(exchangeRateQueryService, walletBalanceService,
                    exchangeTransactionRepository, ledgerEntryRepository);
        }

        @ParameterizedTest
        @EnumSource(value = WalletStatus.class, names = {"FROZEN", "CLOSED"})
        @DisplayName("동결 또는 폐쇄 지갑은 환율·잔액 조회와 저장 전에 거부한다.")
        void rejectsInactiveWallet(WalletStatus status) {
            UserContextHolder.set(new UserContext(USER_ID));
            Wallet wallet = mock(Wallet.class);
            when(walletService.getWallet(WALLET_ID)).thenReturn(wallet);
            when(wallet.getStatus()).thenReturn(status);

            assertInvalidRequest(command(Currency.USD, Currency.KRW, "100"));
            verify(walletService).validateWalletAccess(WALLET_ID, USER_ID);
            verifyNoInteractions(exchangeRateQueryService, walletBalanceService,
                    exchangeTransactionRepository, ledgerEntryRepository);
        }

        @Test
        @DisplayName("환율 조회 실패를 전파하고 잔액 락 조회 및 거래·원장 저장을 수행하지 않는다.")
        void rejectsMissingRate() {
            activeWallet();
            CoreException failure = new CoreException(ErrorType.NOT_FOUND, "환율 정보를 찾을 수 없습니다.");
            when(exchangeRateQueryService.getLatestExchangeRate(Currency.USD, Currency.KRW))
                    .thenThrow(failure);

            assertThatThrownBy(() -> useCase.exchange(command(Currency.USD, Currency.KRW, "100")))
                    .isSameAs(failure);
            verifyNoInteractions(walletBalanceService, exchangeTransactionRepository, ledgerEntryRepository);
        }

        @Test
        @DisplayName("기준 통화 잔액이 부족하면 비즈니스 예외를 던지고, 두 잔액과 거래·원장을 변경하지 않는다.")
        void rejectsInsufficientBalance() {
            Wallet wallet = activeWallet();
            stubRate(Currency.USD, Currency.KRW, "1350.00000000");
            WalletBalance base = stubLockedBalance(wallet, Money.usd("99.99"));
            WalletBalance target = stubLockedBalance(wallet, Money.won(10_000));

            assertThatThrownBy(() -> useCase.exchange(command(Currency.USD, Currency.KRW, "100")))
                    .isInstanceOfSatisfying(CoreException.class, e -> {
                        assertThat(e.getErrorType())
                                .isEqualTo(ErrorType.WALLET_BALANCE_INSUFFICIENT);
                    })
                    .hasMessage("잔액 부족");
            assertThat(base.toMoney()).isEqualTo(Money.usd("99.99"));
            assertThat(target.toMoney()).isEqualTo(Money.won(10_000));
            verifyNoInteractions(exchangeTransactionRepository, ledgerEntryRepository);
        }

    }

    private Wallet activeWallet() {
        UserContextHolder.set(new UserContext(USER_ID));
        Wallet wallet = mock(Wallet.class);
        when(walletService.getWallet(WALLET_ID)).thenReturn(wallet);
        when(wallet.getStatus()).thenReturn(WalletStatus.ACTIVE);
        return wallet;
    }

    private void stubRate(Currency base, Currency target, String value) {
        ExchangeRate rate = ExchangeRate.create(base, target, new BigDecimal(value), RATE_DATE, 2,
                ExchangeRateType.OFFICIAL, ExchangeRateSource.API,
                LocalDateTime.of(2026, 10, 10, 10, 0));
        when(exchangeRateQueryService.getLatestExchangeRate(base, target))
                .thenReturn(ExchangeRateSnapshot.from(rate));
    }

    private WalletBalance stubLockedBalance(Wallet wallet, Money initial) {
        WalletBalance balance = WalletBalance.zero(wallet, initial.currency());
        balance.increase(initial);
        when(walletBalanceService.getWalletBalanceWithLock(WALLET_ID, initial.currency()))
                .thenReturn(balance);
        return balance;
    }

    private ExchangeCommand command(Currency base, Currency target, String amount) {
        return new ExchangeCommand(WALLET_ID, base, target, new BigDecimal(amount), KEY);
    }

    private void assertInvalidRequest(ExchangeCommand command) {
        assertThatThrownBy(() -> useCase.exchange(command))
                .isInstanceOfSatisfying(CoreException.class,
                        e -> assertThat(e.getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    private void assertLockOrder(Currency base, Currency target) {
        var order = inOrder(exchangeRateQueryService, walletBalanceService);
        order.verify(exchangeRateQueryService).getLatestExchangeRate(base, target);
        order.verify(walletBalanceService).getWalletBalanceWithLock(WALLET_ID, Currency.KRW);
        order.verify(walletBalanceService).getWalletBalanceWithLock(WALLET_ID, Currency.USD);
        order.verifyNoMoreInteractions();
    }

    private void assertExchange(ExchangeResult result,
                                WalletBalance base,
                                WalletBalance target,
                                Money baseAmount,
                                Money targetAmount,
                                Money baseAfter,
                                Money targetAfter,
                                String rate) {
        assertThat(base.toMoney()).isEqualTo(baseAfter);
        assertThat(target.toMoney()).isEqualTo(targetAfter);
        verify(walletService).validateWalletAccess(WALLET_ID, USER_ID);

        assertExchangeResult(result, baseAmount, targetAmount, baseAfter, targetAfter, rate);
        assertTransaction(baseAmount, targetAmount, baseAfter, targetAfter, rate);
        assertLedger(baseAmount, targetAmount, baseAfter, targetAfter);
        verifyNoMoreInteractions(exchangeTransactionRepository, ledgerEntryRepository);
    }

    private void assertExchangeResult(ExchangeResult result,
                                      Money baseAmount,
                                      Money targetAmount,
                                      Money baseAfter,
                                      Money targetAfter,
                                      String rate) {
        assertThat(result.walletId()).isEqualTo(WALLET_ID);
        assertThat(result.idempotencyKey()).isEqualTo(KEY);
        assertThat(result.baseMoney()).isEqualTo(baseAmount);
        assertThat(result.targetMoney()).isEqualTo(targetAmount);
        assertThat(result.baseBalanceAfter()).isEqualTo(baseAfter);
        assertThat(result.targetBalanceAfter()).isEqualTo(targetAfter);
        assertThat(result.appliedRate()).isEqualByComparingTo(rate);
        assertThat(result.rateDate()).isEqualTo(RATE_DATE);
        assertThat(result.roundNo()).isEqualTo(2);
        assertThat(result.rateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(result.rateSource()).isEqualTo(ExchangeRateSource.API);
    }

    private void assertTransaction(Money baseAmount,
                                   Money targetAmount,
                                   Money baseAfter,
                                   Money targetAfter,
                                   String rate) {
        ArgumentCaptor<ExchangeTransaction> transactionCaptor = ArgumentCaptor.forClass(ExchangeTransaction.class);
        verify(exchangeTransactionRepository).save(transactionCaptor.capture());

        ExchangeTransaction transaction = transactionCaptor.getValue();
        assertThat(transaction.getWalletId()).isEqualTo(WALLET_ID);
        assertThat(transaction.getIdempotencyKey()).isEqualTo(KEY);
        assertThat(transaction.getBaseCurrency()).isEqualTo(baseAmount.currency());
        assertThat(transaction.getTargetCurrency()).isEqualTo(targetAmount.currency());
        assertThat(transaction.getBaseAmount()).isEqualByComparingTo(baseAmount.amount());
        assertThat(transaction.getTargetAmount()).isEqualByComparingTo(targetAmount.amount());
        assertThat(transaction.getBaseBalanceAfter()).isEqualByComparingTo(baseAfter.amount());
        assertThat(transaction.getTargetBalanceAfter()).isEqualByComparingTo(targetAfter.amount());
        assertThat(transaction.getAppliedRate()).isEqualByComparingTo(rate);
        assertThat(transaction.getAppliedRate().scale()).isEqualTo(8);
        assertThat(transaction.getRateDate()).isEqualTo(RATE_DATE);
        assertThat(transaction.getRoundNo()).isEqualTo(2);
        assertThat(transaction.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(transaction.getRateSource()).isEqualTo(ExchangeRateSource.API);
    }

    private void assertLedger(Money baseAmount,
                              Money targetAmount,
                              Money baseAfter,
                              Money targetAfter) {
        ArgumentCaptor<LedgerEntry> ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository, times(2)).save(ledgerCaptor.capture());

        assertLedgerEntry(ledgerCaptor.getAllValues().get(0), baseAmount, baseAfter,
                EntryType.FX_WITHDRAW, Direction.DEBIT);

        assertLedgerEntry(ledgerCaptor.getAllValues().get(1), targetAmount, targetAfter,
                EntryType.FX_DEPOSIT, Direction.CREDIT);
    }

    private void assertLedgerEntry(LedgerEntry entry,
                                    Money amount,
                                    Money after,
                                    EntryType type,
                                    Direction direction) {
        assertThat(entry.getWalletId()).isEqualTo(WALLET_ID);
        assertThat(entry.getCurrency()).isEqualTo(amount.currency());
        assertThat(entry.getAmount()).isEqualByComparingTo(amount.amount());
        assertThat(entry.getBalanceAfter()).isEqualByComparingTo(after.amount());
        assertThat(entry.getEntryType()).isEqualTo(type);
        assertThat(entry.getDirection()).isEqualTo(direction);
    }
}
