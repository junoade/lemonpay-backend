package com.lemonpay.payment.application;

import com.lemonpay.common.auth.UserContext;
import com.lemonpay.common.auth.UserContextHolder;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberRepository;
import com.lemonpay.merchant.domain.Merchant;
import com.lemonpay.merchant.domain.MerchantRepository;
import com.lemonpay.payment.domain.PaymentStatus;
import com.lemonpay.payment.domain.PaymentTransaction;
import com.lemonpay.payment.domain.PaymentTransactionRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@Transactional
@SpringBootTest
class PaymentUseCaseIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PaymentUseCase paymentUseCase;
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    private Member member;
    private Wallet wallet;
    private Merchant merchant;
    private String today;

    @BeforeEach
    void setUp() {
        member = saveMember();
        wallet = saveWallet(member);
        merchant = saveMerchant();
        today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        log.info("Outer @BeforeEach, save data");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
        log.info("Outer @AfterEach, tearDown");
    }

    private Member saveMember() {
        String email = "lemonpay@gmail.com";
        String name = "LemonPay";
        String phone = "+821012341234";
        Member member = Member.create(email, name, phone);
        return memberRepository.save(member);
    }

    private Wallet saveWallet(Member member) {
        String walletName = "WalletTest";
        String productCode = "BASC-V1";
        Wallet wallet = Wallet.create(member, walletName, productCode);
        return walletRepository.save(wallet);
    }

    private Merchant saveMerchant() {
        String name = "MerchantTest";
        String callbackUrl = "https://www.merchant.com/exmaple/callback";
        Merchant merchant = Merchant.create(name, callbackUrl);
        return merchantRepository.save(merchant);
    }

    @Nested
    @DisplayName("결제 요청 통합 테스트")
    class PaymentCreation {
        // 결제 요청 테스트 정상
        @Test
        @DisplayName("결제 요청 시 요청 상태의 결제 내역이 정상적으로 생성된다")
        void createPayment_thenSuccess() {
            // given
            UserContextHolder.set(new UserContext(member.getId()));
            var command = createKrwPaymentCommand();

            // when
            PaymentResult result = paymentUseCase.createPendingPayment(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.txNo()).isNotBlank();
            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING.name());
            assertThat(result.txNo()).startsWith(today);
            assertThat(result.txNo()).hasSize(18);

            PaymentTransaction tx = paymentTransactionRepository.findByTxNo(result.txNo()).orElseThrow();
            assertThat(tx.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(tx.getWalletId()).isEqualTo(wallet.getId());
            assertThat(tx.getMerchantId()).isEqualTo(merchant.getId());
            assertThat(tx.getAmount()).isEqualByComparingTo(command.amount());
            assertThat(tx.getCurrency()).isEqualTo(command.currency());
            assertThat(tx.getSettlementAmount()).isEqualByComparingTo(command.settlementAmount());
            assertThat(tx.getSettlementCurrency()).isEqualTo(command.settlementCurrency());
            assertThat(tx.getExchangeRate()).isEqualTo(command.exchangeRate());
            assertThat(tx.getOrderId()).isEqualTo(command.orderId());

        }

        @Test
        @DisplayName("결제 요청시 지갑 소유주가 다르면 예외가 발생한다.")
        void createPayment_throwsValidationException() {
            // given
            UUID otherUserId = UUID.randomUUID();
            UserContextHolder.set(new UserContext(otherUserId));
            var command = createKrwPaymentCommand();
            // when & then
            assertThatThrownBy(() -> paymentUseCase.createPendingPayment(command))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.FORBIDDEN);
        }

        @Test
        @DisplayName("결제 요청 시 가맹점이 ACTIVE 상태가 아닌, SUSPENDID 등의 상태이면 예외가 발생한다.")
        void createPayment_throwsMerchantStateException() {
            // given
            UserContextHolder.set(new UserContext(member.getId()));
            merchant.close();
            var command = createKrwPaymentCommand();

            // when & then
            assertThatThrownBy(() -> paymentUseCase.createPendingPayment(command))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.INVALID_REQUEST);
        }

        private PaymentCommand.Create createKrwPaymentCommand() {
            Currency purchasedCurrency = Currency.KRW;
            Currency settlementCurrency = Currency.KRW;
            Money purchaseAmount = Money.of(1000, purchasedCurrency);
            Money settlementAmount = Money.of(1000, settlementCurrency);
            BigDecimal exchangeRate = BigDecimal.ONE;
            String orderId = today + "ORDER" + UUID.randomUUID();
            String idempotencyKey = today + UUID.randomUUID();

            return new PaymentCommand.Create(
                    wallet.getId(),
                    merchant.getId(),
                    purchaseAmount.currency(),
                    purchaseAmount.amount(),
                    settlementAmount.currency(),
                    settlementAmount.amount(),
                    exchangeRate,
                    orderId,
                    idempotencyKey
            );
        }
    }

    @Nested
    @DisplayName("결제 승인 통합 테스트")
    class PaymentApprove {
        // 결제 승인 테스트 정상

        // 결제 승인 테스트 - 결제생성건이 없을때
        // 결제 승인 테스트 - 이미 결제 처리가 되었을때
        // 결제 승인 테스트 예외) 지갑 소유주 다를 때
        // 결제 승인 테스트 - 가맹점이 정지 되었을때
    }

    @Nested
    @DisplayName("결제 취소 통합 테스트")
    class PaymentCancel {

    }

    @Nested
    @DisplayName("결제 조회 통합 테스트")
    class PaymentView {

    }
}