package com.lemonpay.wallet.application;


import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceService;
import com.lemonpay.wallet.domain.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChargeUseCase {

   private final WalletService walletService;
   private final WalletBalanceService walletBalanceService;
   private final LedgerEntryRepository ledgerEntryRepository;

   private static final BigDecimal MIN_KRW_CHARGE_AMOUNT = BigDecimal.valueOf(1_000);
   private static final BigDecimal MAX_KRW_CHARGE_AMOUNT = BigDecimal.valueOf(1_000_000);

   @Transactional
   public ChargeResult charge(UUID walletId, Currency currency, Money money) {
       Wallet wallet = walletService.getWallet(walletId);
       wallet.validateChargeable();
       validateKrwCharge(money);

       WalletBalance walletBalance = walletBalanceService.getWalletBalance(walletId, currency);
       walletBalance.increase(money);

       Money balanceAfter = walletBalance.toMoney();
       LedgerEntry ledgerEntry = LedgerEntry.of(walletId, money, balanceAfter, EntryType.CHARGE);
       ledgerEntryRepository.save(ledgerEntry);

       return ChargeResult.of(walletId, money, walletBalance);
   }


    /**
     * 충전 금액에 대한 정책을 검증한다.
     *
     * <p>현재는 ChargeUseCase 내부에서만 사용되는 단순 정책이므로 private 메소드로 유지한다.
     * 다통화 충전, 정책 변경(최소/최대 금액, 통화별 규칙 등)으로 재사용 필요성이 생기면,
     * 별도의 ChargePolicy 클래스로 분리한다. </p>
     * @param money
     */
    private void validateKrwCharge(Money money) {
        if(money.currency() != Currency.KRW) {
            throw new CoreException(ErrorType.INVALID_CURRENCY);
        }

        if(money.amount().compareTo(MIN_KRW_CHARGE_AMOUNT) < 0) {
            throw new CoreException(ErrorType.INVALID_CHARGE_AMOUNT,
                    "최소 충전 금액은 %s원입니다.".formatted(MIN_KRW_CHARGE_AMOUNT.toPlainString()));
        }

        if(money.amount().compareTo(MAX_KRW_CHARGE_AMOUNT) > 0) {
            throw new CoreException(ErrorType.INVALID_CHARGE_AMOUNT,
                    "최대 충전 금액은 %s원입니다.".formatted(MAX_KRW_CHARGE_AMOUNT.toPlainString()));
        }
    }
}
