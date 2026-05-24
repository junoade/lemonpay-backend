package com.lemonpay.payment.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_transactions_tx_no",
                        columnNames = "tx_no"
                ),
                @UniqueConstraint(name = "uk_payment_transactions_merchant_idempotency",
                        columnNames = {"merchant_id", "idempotency_key"}
                )
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // AUTO_INCREMENT, 내부 전용

    @Column(name = "tx_no", nullable = false, updatable = false, length = 18)
    private String txNo; // 거래일련번호: yyyyMMdd + 10자리 순번

    @Column(name = "amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency; // 결제 통화

    @Column(name = "settlement_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal settlementAmount; // 실제 결제 통화 (KRW | USD| JPY)

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_currency", nullable = false, length = 3)
    private Currency settlementCurrency; // 실제 차감 통화 e.g) KRW

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 4)
    private BigDecimal exchangeRate; // 적용 환율

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status; // 결제 상태 PENDING | COMPLETED | FAILED | CANCELLED

    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 100)
    private String idempotencyKey; // 중복 결제 방지

    @Column(name = "order_id", nullable = true, length = 100)
    private String orderId;

    @Column(name = "wallet_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID walletId; // Wallet.id 참조. 지갑 ID

    @Column(name = "merchant_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID merchantId; // Merchant.id 참조. 가맹점 ID

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 결제 완료 시각, nullable

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 결제 완료 시각, nullable

    private PaymentTransaction(
            String txNo,
            UUID walletId,
            UUID merchantId,
            BigDecimal amount,
            Currency currency,
            BigDecimal settlementAmount,
            Currency settlementCurrency,
            BigDecimal exchangeRate,
            String idempotencyKey,
            String orderId
    ) {
        this.txNo = txNo;
        this.walletId = walletId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.settlementAmount = settlementAmount;
        this.settlementCurrency = settlementCurrency;
        this.exchangeRate = exchangeRate;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
        this.orderId = orderId;
    }

    public static PaymentTransaction create(
            String txNo,
            UUID walletId,
            UUID merchantId,
            BigDecimal amount,
            Currency currency,
            BigDecimal settlementAmount,
            Currency settlementCurrency,
            BigDecimal exchangeRate,
            String idempotencyKey,
            String orderId
    ) {
        return new PaymentTransaction(
                txNo,
                walletId,
                merchantId,
                amount,
                currency,
                settlementAmount,
                settlementCurrency,
                exchangeRate,
                idempotencyKey,
                orderId
        );
    }

    public void cancel() {
        this.status.validateTransition(PaymentStatus.CANCELLED);
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status.validateTransition(PaymentStatus.COMPLETED);
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void failed() {
        this.status.validateTransition(PaymentStatus.FAILED);
        this.status = PaymentStatus.FAILED;
    }

    public Money toPaymentMoney() {
        return Money.of(amount, currency);
    }
}
