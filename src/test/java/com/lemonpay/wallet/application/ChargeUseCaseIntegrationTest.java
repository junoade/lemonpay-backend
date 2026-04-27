package com.lemonpay.wallet.application;

import com.lemonpay.common.auth.UserContext;
import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.ledger.domain.Direction;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberRepository;
import com.lemonpay.wallet.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class ChargeUseCaseIntegrationTest {

    @Autowired private ChargeUseCase chargeUseCase;
    @Autowired private MemberRepository memberRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletBalanceService walletBalanceService;
    @Autowired private WalletBalanceRepository walletBalanceRepository;
    @Autowired private LedgerEntryRepository ledgerEntryRepository;

    private Member savedMember;
    private Wallet savedWallet;
    private WalletBalance savedWalletBalance;

    private static final BigDecimal MAX_KRW_CHARGE_AMOUNT = BigDecimal.valueOf(1_000_000);

    @BeforeEach
    public void init() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        int randomNumber = ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
        savedMember = memberRepository.save(
                Member.create(
                        "test-" + suffix + "@test.com",
                        "test-" + suffix,
                        "+8210" + randomNumber
                )
        );

        savedWallet = walletRepository.save(Wallet.create(savedMember, "testWallet", "VASC-V1"));
        savedWalletBalance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();
    }

    @AfterEach
    public void clean() {
        UserContextHolder.clear();
    }


    @Test
    @DisplayName("KRW 가 정상적으로 충전되면 KRW 지갑 잔액이 충전 금액 만큼 증가하고 원장 기록도 생성된다.")
    void chargeSuccess_thenBalanceUpdateAndLedgerEntryInsertCorrectly() {
        // given
        UserContextHolder.set(new UserContext(savedMember.getId()));
        Money chargeAmount = Money.won(1_000_000);
        Money beforeChargeAmount = savedWalletBalance.toMoney();

        // when
        ChargeResult chargeResult = chargeUseCase.charge(savedWallet.getId(), chargeAmount);

        // then 1 - 반환값 검증
        assertThat(chargeResult).isNotNull();
        assertThat(chargeResult.walletId()).isEqualTo(savedWallet.getId());
        assertThat(chargeResult.chargeMoney()).isEqualTo(chargeAmount);

        // then 2 - 잔액 검증
        WalletBalance afterBalance = walletBalanceService.getWalletBalance(savedWallet.getId(), Currency.KRW);
        assertThat(afterBalance.getBalance()).isEqualByComparingTo(beforeChargeAmount.amount().add(chargeAmount.amount()));

        // then 3 - 원장 검증
        Page<LedgerEntry> ledgerPage =
                ledgerEntryRepository.findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10));

        assertThat(ledgerPage.getContent()).hasSize(1);

        LedgerEntry ledgerEntry = ledgerPage.getContent().get(0);
        assertThat(ledgerEntry.getWalletId()).isEqualTo(savedWallet.getId());
        assertThat(ledgerEntry.getCurrency()).isEqualTo(Currency.KRW);
        assertThat(ledgerEntry.getAmount()).isEqualByComparingTo(chargeAmount.amount());
        assertThat(ledgerEntry.getBalanceAfter()).isEqualByComparingTo(chargeAmount.amount());
        assertThat(ledgerEntry.getEntryType()).isEqualTo(EntryType.CHARGE);
        assertThat(ledgerEntry.getDirection()).isEqualTo(Direction.CREDIT);
    }

    @Test
    @DisplayName("다른 사용자의 지갑을 충전하려고 하면 접근 거부 예외가 발생하며 DB 반영은 없다.")
    void chargeThrowsException_whenTryingToChargeOthers() {
        // given
        UserContextHolder.set(new UserContext(UUID.randomUUID()));
        Money chargeAmount = Money.won(1_000_000);
        BigDecimal beforeChargeAmount = savedWalletBalance.getBalance();
        int beforeLedgerSize = ledgerEntryRepository
                .findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10))
                .getContent()
                .size();

        // when & then
        assertThatThrownBy(() -> chargeUseCase.charge(savedWallet.getId(), chargeAmount))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.FORBIDDEN);

        WalletBalance afterBalance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();

        int afterLedgerSize = ledgerEntryRepository
                .findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10))
                .getContent()
                .size();

        assertThat(afterBalance.getBalance()).isEqualByComparingTo(beforeChargeAmount);
        assertThat(afterLedgerSize).isEqualTo(beforeLedgerSize);
    }

    @Test
    @DisplayName("유효하지 않은 충전 정책으로 충전하려고 할 때, 예외가 발생하고 DB 반영은 없다.")
    void chargeThrowsException_whenTryingWithInvalidPolicy() {
        // given
        UserContextHolder.set(new UserContext(savedMember.getId()));
        Money chargeAmount = Money.of(MAX_KRW_CHARGE_AMOUNT.add(BigDecimal.valueOf(1000)), Currency.KRW);

        BigDecimal beforeChargeAmount = savedWalletBalance.getBalance();
        int beforeLedgerSize = ledgerEntryRepository
                .findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10))
                .getContent()
                .size();

        // when & then
        assertThatThrownBy(() -> chargeUseCase.charge(savedWallet.getId(), chargeAmount))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.INVALID_CHARGE_AMOUNT);

        WalletBalance afterBalance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();

        int afterLedgerSize = ledgerEntryRepository
                .findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10))
                .getContent()
                .size();

        assertThat(afterBalance.getBalance()).isEqualByComparingTo(beforeChargeAmount);
        assertThat(afterLedgerSize).isEqualTo(beforeLedgerSize);

    }


    @Test
    @DisplayName("동시 충전 요청 시 낙관적 락 충돌이 발생하면 성공 건만 잔액과 원장에 반영된다. (retry 로직 없음)")
    void chargeConcurrencyTest() throws InterruptedException {
        // given
        int tryCount = 2;
        Money chargeAmount = Money.won(10_000);
        BigDecimal beforeAmount = savedWalletBalance.getBalance();

        ExecutorService executor = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tryCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < tryCount; i++) {
            executor.submit(() -> {
                try {
                    UserContextHolder.set(new UserContext(savedMember.getId()));
                    startLatch.await();

                    chargeUseCase.charge(savedWallet.getId(), chargeAmount);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                    log.warn("낙관적 락 충돌 - 동시성 제어 정상 작동함");
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    UserContextHolder.clear();
                    doneLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        try {
            assertThat(completed).isTrue(); // 타임아웃 자체도 실패 처리
        } finally {
            // assertion 실패에 따른 쓰레드 미반환 대응
            executor.shutdown();
        }

        // then 1 - 성공/실패 건수
        assertThat(successCount.get() + failCount.get()).isEqualTo(tryCount);
        assertThat(successCount.get()).isBetween(1, tryCount);

        // then 2 - 최종 잔액 검증
        WalletBalance afterBalance = walletBalanceRepository
                .findByWalletIdAndCurrency(savedWallet.getId(), Currency.KRW)
                .orElseThrow();
        assertThat(afterBalance.getBalance())
                .isEqualByComparingTo(
                        beforeAmount.add(chargeAmount.amount().multiply(BigDecimal.valueOf(successCount.get())))
                );

        // then 3 - 원장 건수 검증
        Page<LedgerEntry> ledgerPage =
                ledgerEntryRepository.findByWalletIdOrderByIdDesc(savedWallet.getId(), PageRequest.of(0, 10));
        assertThat(ledgerPage.getContent()).hasSize(successCount.get());

    }

    // TODO(idempotency): 동일 거래 중복 요청 시 멱등성 보장 테스트 추가
    // TODO(retry): 네트워크 지연/타임아웃으로 인한 재시도 시 잔액 중복 증가 방지 테스트 추가

}
