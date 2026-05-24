package com.lemonpay.payment.application;

import com.lemonpay.common.auth.UserContext;
import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberRepository;
import com.lemonpay.merchant.domain.Merchant;
import com.lemonpay.merchant.domain.MerchantRepository;
import com.lemonpay.payment.domain.PaymentStatus;
import com.lemonpay.payment.domain.PaymentTransaction;
import com.lemonpay.payment.domain.PaymentTransactionRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletBalanceRepository;
import com.lemonpay.wallet.domain.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentUseCaseConcurrencyTest {

    @Autowired private PaymentUseCase paymentUseCase;
    @Autowired private MemberRepository memberRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletBalanceRepository walletBalanceRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private PaymentTransactionRepository paymentTransactionRepository;
    @Autowired private LedgerEntryRepository ledgerEntryRepository;

    private Member savedMember;
    private Wallet savedWallet;
    private Merchant savedMerchant;

    @BeforeEach
    void init() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        int randomNumber = ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);

        savedMember = memberRepository.save(
                Member.create(
                        "payment-" + suffix + "@test.com",
                        "payment-" + suffix,
                        "+8210" + randomNumber
                )
        );
        savedWallet = walletRepository.save(Wallet.create(savedMember, "paymentWallet", "BASC-V1"));
        savedMerchant = merchantRepository.save(
                Merchant.create("payment-store-" + suffix, "https://merchant.test/callback")
        );

        WalletBalance balance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();
        balance.increase(com.lemonpay.common.domain.Money.won(50_000));
        walletBalanceRepository.save(balance);
    }

    @AfterEach
    void clean() {
        UserContextHolder.clear();
    }


    @Test
    @DisplayName("동일 txNo 결제를 동시에 승인하면 1건만 성공하고 잔액과 원장은 1회만 반영된다.")
    void approvePaymentConcurrently_thenOnlyOneApprovalIsApplied() throws InterruptedException {
        // given
        UserContextHolder.set(new UserContext(savedMember.getId()));
        PaymentResult pendingPayment = paymentUseCase.createPendingPayment(new PaymentCommand.Create(
                savedWallet.getId(),
                savedMerchant.getId(),
                Currency.KRW,
                BigDecimal.valueOf(10_000),
                Currency.KRW,
                BigDecimal.valueOf(10_000),
                BigDecimal.ONE,
                "order-" + UUID.randomUUID(),
                "idempotency-" + UUID.randomUUID()
        ));
        UserContextHolder.clear();

        int tryCount = 2;
        BigDecimal beforeAmount = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow()
                .getBalance();

        ExecutorService executor = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tryCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        Queue<Throwable> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < tryCount; i++) {
            executor.submit(() -> {
                try {
                    UserContextHolder.set(new UserContext(savedMember.getId()));
                    startLatch.await();

                    paymentUseCase.approvePayment(new PaymentCommand.Approve(pendingPayment.txNo()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    failures.add(e);
                } finally {
                    UserContextHolder.clear();
                    doneLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();

        // then
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(tryCount - 1);
        assertThat(failures).isNotEmpty();
        assertThat(failures).anySatisfy(this::assertExpectedConcurrencyException);

        PaymentTransaction tx = paymentTransactionRepository.findByTxNo(pendingPayment.txNo()).orElseThrow();
        assertThat(tx.getStatus()).isEqualTo(PaymentStatus.COMPLETED);

        WalletBalance afterBalance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();
        assertThat(afterBalance.getBalance()).isEqualByComparingTo(beforeAmount.subtract(BigDecimal.valueOf(10_000)));

        Page<LedgerEntry> paymentLedgers =
                ledgerEntryRepository.findByWalletIdAndCurrencyAndEntryTypeOrderByIdDesc(
                        savedWallet.getId(),
                        Currency.KRW,
                        EntryType.PAYMENT,
                        PageRequest.of(0, 10)
                );
        assertThat(paymentLedgers.getContent()).hasSize(1);
        assertThat(paymentLedgers.getContent().get(0).getAmount()).isEqualByComparingTo("10000");
    }

    private void assertExpectedConcurrencyException(Throwable failure) {
        assertThat(failure)
                .satisfiesAnyOf(
                        e -> assertThat(e).isInstanceOf(ObjectOptimisticLockingFailureException.class),
                        e -> assertThat(e).isInstanceOf(OptimisticLockingFailureException.class),
                        e -> assertThat(e).isInstanceOf(CoreException.class)
                                .extracting("errorType")
                                .isEqualTo(ErrorType.INVALID_STATE_TRANSITION)

                );
    }
}
