package com.lemonpay.payment.interfaces.api;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.payment.application.PaymentCommand;
import com.lemonpay.payment.application.PaymentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "결제 API 요청 및 응답 DTO")
public class PaymentDto {

    @Schema(description = "결제 세션 생성 요청")
    public record CreationRequest(
            @NotNull UUID walletId,
            @NotNull UUID merchantId,
            @NotBlank String currency,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String orderId,
            @NotBlank String idempotencyKey
    ) {
        public PaymentCommand.Create toCommand() {
            Currency parsedCurrency = Currency.valueOf(currency());
            return new PaymentCommand.Create(
                    walletId(),
                    merchantId(),
                    parsedCurrency,
                    amount(),
                    parsedCurrency, // TODO
                    amount(), // TODO
                    BigDecimal.ZERO,
                    orderId(),
                    idempotencyKey()
            );
        }
    }

    @Schema(description = "결제 승인 요청 - MVP 범위에서는 별도의 body 없이 X-USER-ID와 paymentId로 승인 처리")
    public record ApproveRequest() {
    }

    @Schema(description = "결제 취소 요청")
    public record CancelRequest(
    ) { }

    @Schema(description = "결제 거래 응답")
    public record PaymentResponse(
            @Schema(description = "레몬페이 결제 거래번호", example = "202601010000000001")
            String txNo,

            @Schema(description = "결제 상태", example = "PENDING")
            String status,

            @Schema(description = "지갑 ID")
            UUID walletId,

            @Schema(description = "가맹점 ID")
            UUID merchantId,

            @Schema(description = "가맹점명", example = "테스트 가맹점")
            String merchantName,

            @Schema(description = "결제 금액", example = "10000.0000")
            BigDecimal amount,

            @Schema(description = "결제 통화", example = "KRW")
            String currency,

            @Schema(description = "정산 금액", example = "10000.0000")
            BigDecimal settlementAmount,

            @Schema(description = "정산 통화", example = "KRW")
            String settlementCurrency,

            @Schema(description = "적용 환율", example = "1.0000")
            BigDecimal exchangeRate,

            @Schema(description = "결제 요청 생성 시각")
            LocalDateTime createdAt,

            @Schema(description = "결제 요청 완료 시각, 완료 전이면 null")
            LocalDateTime completedAt,

            @Schema(description = "결제 요청 취소 시각, 취소 전이면 null")
            LocalDateTime cancelledAt
    ) {
        public static PaymentDto.PaymentResponse from(PaymentResult result) {
            return new PaymentDto.PaymentResponse(
                    result.txNo(),
                    result.status(),
                    result.walletId(),
                    result.merchantId(),
                    result.merchantName(),
                    result.amount(),
                    result.currency(),
                    result.settlementAmount(),
                    result.settlementCurrency(),
                    result.exchangeRate(),
                    result.createdAt(),
                    result.completedAt(),
                    result.cancelledAt()
            );
        }
    }
}
