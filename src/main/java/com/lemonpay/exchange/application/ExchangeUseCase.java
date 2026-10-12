package com.lemonpay.exchange.application;

import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.exchange.domain.ExchangeQuote;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeTransaction;
import com.lemonpay.exchange.domain.ExchangeTransactionRepository;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.wallet.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExchangeUseCase {
    private final WalletService walletService;
    private final WalletBalanceService walletBalanceService;
    private final ExchangeRateQueryService exchangeRateQueryService;
    private final ExchangeTransactionRepository exchangeTransactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public ExchangeResult exchange(ExchangeCommand command) {
        validateInput(command);
        validateOwnedWallet(command.walletId());

        ExchangeRate rate = exchangeRateQueryService
                .getLatestExchangeRate(command.baseCurrency(), command.targetCurrency())
                .toExchangeRate();

        ExchangeQuote quote = ExchangeQuote.fromBaseAmount(
                Money.of(command.baseAmount(), command.baseCurrency()), rate);
        validateQuote(quote);

        Map<Currency, WalletBalance> locked = lockBalances(command);
        WalletBalance base = locked.get(command.baseCurrency());
        WalletBalance target = locked.get(command.targetCurrency());

        base.decrease(quote.baseMoney());
        target.increase(quote.targetMoney());

        recordFxHistory(command, quote, rate, base.toMoney(), target.toMoney());
        recordLedger(command.walletId(), quote, base, target);

        // commit
        return new ExchangeResult(
                command.walletId(),
                quote.baseMoney(),
                quote.targetMoney(),
                base.toMoney(),
                target.toMoney(),
                quote.appliedRate(),
                rate.getRateDate(),
                rate.getRoundNo(),
                rate.getRateType(),
                rate.getSource(),
                command.idempotencyKey()
        );
    }

    private void validateInput(ExchangeCommand command) {
        if (command == null || command.walletId() == null
                || command.baseCurrency() == null || command.targetCurrency() == null
                || command.baseAmount() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "환전 요청 필수값을 확인해주세요.");
        }
        if (command.baseCurrency() == command.targetCurrency()) {
            throw new CoreException(ErrorType.INVALID_REQUEST,
                    "기준 통화와 대상 통화는 달라야 합니다.");
        }
        if (command.baseAmount().signum() <= 0) {
            throw new CoreException(ErrorType.INVALID_REQUEST,
                    "환전 금액은 0보다 커야 합니다.");
        }
    }

    private void validateOwnedWallet(UUID walletId) {
        UUID userId = UserContextHolder.getUserId();
        walletService.validateWalletAccess(walletId, userId);
        Wallet wallet = walletService.getWallet(walletId);
        if(wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "환전할 수 없는 지갑 상태입니다.");
        }
    }

    private void validateQuote(ExchangeQuote quote) {
        if (quote.baseMoney().isZero() || quote.targetMoney().isZero()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "통화 단위에 맞춘 환전 금액은 0보다 커야 합니다.");
        }
    }

    private Map<Currency, WalletBalance> lockBalances(ExchangeCommand command) {
        List<Currency> lockOrder = Stream.of(command.baseCurrency(), command.targetCurrency())
                .sorted(Comparator.comparing(Currency::getCode))
                .toList();

        // WallectBalance 변경 (비관적 락)
        Map<Currency, WalletBalance> locked = new EnumMap<>(Currency.class);
        for(Currency currency : lockOrder) {
            WalletBalance balance = walletBalanceService.getWalletBalanceWithLock(command.walletId(), currency);
            locked.put(currency, balance);
        }

        return locked;
    }

    private ExchangeTransaction recordFxHistory(
            ExchangeCommand command,
            ExchangeQuote quote,
            ExchangeRate rate,
            Money baseBalanceAfter,
            Money targetBalanceAfter) {
        ExchangeTransaction exchangeTransaction = ExchangeTransaction.completed(
                command.walletId(),
                command.idempotencyKey(),
                quote,
                rate,
                baseBalanceAfter,
                targetBalanceAfter
        );
        return exchangeTransactionRepository.save(exchangeTransaction);
    }

    private void recordLedger(UUID walletId, ExchangeQuote quote, WalletBalance base, WalletBalance target) {
        saveLedgerEntry(walletId, quote.baseMoney(), base.toMoney(), EntryType.FX_WITHDRAW);
        saveLedgerEntry(walletId, quote.targetMoney(), target.toMoney(), EntryType.FX_DEPOSIT);

    }

    private void saveLedgerEntry(UUID walletId, Money amount, Money balanceAfter, EntryType entryType) {
        ledgerEntryRepository.save(
                LedgerEntry.of(walletId,
                        amount,
                        balanceAfter,
                        entryType
                )
        );
    }
}
