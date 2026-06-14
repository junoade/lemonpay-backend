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
        var userId = UserContextHolder.getUserId();
        walletService.validateWalletAccess(command.walletId(), userId);
        var merchant = merchantService.getPayableMerchant(command.merchantId());

        String txNo = txNoGenerator.generate();
        Money paymentAmount = Money.of(command.amount(), command.currency());
        Money settlementAmount = Money.of(command.settlementAmount(), command.settlementCurrency());

        var result = paymentTransactionService.createPending(
                txNo,
                command.walletId(),
                command.merchantId(),
                paymentAmount,
                settlementAmount,
                command.exchangeRate(),
                command.idempotencyKey(),
                command.orderId()
        );

        return PaymentResult.of(result, merchant.getName());
    }

    @Transactional
    public PaymentResult approvePayment(PaymentCommand.Approve command) {
        PaymentTransaction tx = getOwnedPaymentTransaction(command.txNo());
        var merchant = merchantService.getPayableMerchant(tx.getMerchantId());
        paymentTransactionService.validateCompletable(tx);
        var balance = debitPaymentAmount(tx);
        recordPaymentLedger(tx, balance);
        tx.complete();
        return PaymentResult.of(tx, merchant.getName());
    }

    @Transactional(readOnly = true)
    public PaymentResult getDetail(PaymentCommand.Query command) {
        PaymentTransaction tx = getOwnedPaymentTransaction(command.txNo());
        var merchant = merchantService.getMerchantById(tx.getMerchantId());
        return PaymentResult.of(tx, merchant.getName());
    }

    @Transactional
    public PaymentResult cancelPayment(PaymentCommand.Cancel command) {
        PaymentTransaction tx = getOwnedPaymentTransaction(command.txNo());
        tx.cancel();
        var merchant = merchantService.getMerchantById(tx.getMerchantId());
        return PaymentResult.of(tx, merchant.getName());
    }

    private PaymentTransaction getOwnedPaymentTransaction(String txNo) {
        var userId = UserContextHolder.getUserId();
        PaymentTransaction tx = paymentTransactionService.getByTxNo(txNo);
        walletService.validateWalletAccess(tx.getWalletId(), userId);
        return tx;
    }

    private WalletBalance debitPaymentAmount(PaymentTransaction tx) {
        Money paymentAmount = tx.toPaymentMoney();
        WalletBalance balance = walletBalanceService.getWalletBalance(tx.getWalletId(), tx.getCurrency());
        balance.decrease(paymentAmount);
        return balance;
    }

    private void recordPaymentLedger(PaymentTransaction tx, WalletBalance balance) {
        LedgerEntry ledgerEntry = LedgerEntry.of(
                tx.getWalletId(),
                tx.toPaymentMoney(),
                balance.toMoney(),
                EntryType.PAYMENT
        );
        ledgerEntryRepository.save(ledgerEntry);
    }
}
