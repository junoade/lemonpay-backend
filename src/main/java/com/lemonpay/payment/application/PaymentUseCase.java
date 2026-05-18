package com.lemonpay.payment.application;

import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Money;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.merchant.domain.MerchantService;
import com.lemonpay.payment.domain.PaymentTransaction;
import com.lemonpay.payment.domain.PaymentTransactionService;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceService;
import com.lemonpay.wallet.domain.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentUseCase {

    private final PaymentTransactionService paymentTransactionService;
    private final MerchantService merchantService;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletService walletService;
    private final PaymentTxNoGenerator txNoGenerator;
    private final WalletBalanceService walletBalanceService;

    @Transactional
    public PaymentResult createPendingPayment(PaymentCommand.Create command) {
        merchantService.validateMerchantPayable(command.merchantId());
        var merchant = merchantService.getMerchantById(command.merchantId());

        walletService.validateWalletAccess(command.walletId(), UserContextHolder.getUserId());

        String txNo = txNoGenerator.generate();
        var result = paymentTransactionService.createPending(
                txNo,
                command.walletId(),
                command.merchantId(),
                Money.of(command.amount(), command.currency()),
                Money.of(command.settlementAmount(), command.settlementCurrency()),
                command.exchangeRate(),
                command.idempotencyKey(),
                command.orderId()
        );

        return PaymentResult.of(result, merchant.getName());
    }

    @Transactional
    public PaymentResult approvePayment(PaymentCommand.Approve command) {
        var userId = UserContextHolder.getUserId();

        PaymentTransaction tx = paymentTransactionService.getByTxNo(command.txNo());

        walletService.validateWalletAccess(tx.getWalletId(), userId);
        merchantService.validateMerchantPayable(tx.getMerchantId());
        var merchant = merchantService.getMerchantById(tx.getMerchantId());

        paymentTransactionService.validateCompletable(tx);
        Money paymentAmount = Money.of(tx.getAmount(), tx.getCurrency());
        WalletBalance balance = walletBalanceService.getWalletBalance(tx.getWalletId(), tx.getCurrency());

        balance.decrease(paymentAmount);

        LedgerEntry ledgerEntry = LedgerEntry.of(
                tx.getWalletId(),
                paymentAmount,
                balance.toMoney(),
                EntryType.PAYMENT
        );
        ledgerEntryRepository.save(ledgerEntry);

        tx.complete();
        return PaymentResult.of(tx, merchant.getName());
    }

    @Transactional(readOnly = true)
    public PaymentResult getDetail(PaymentCommand.Query command) {
        var userId = UserContextHolder.getUserId();

        PaymentTransaction tx = paymentTransactionService.getByTxNo(command.txNo());
        walletService.validateWalletAccess(tx.getWalletId(), userId);
        var merchant = merchantService.getMerchantById(tx.getMerchantId());

        return PaymentResult.of(tx, merchant.getName());
    }
}
