package com.lemonpay.payment.interfaces.api;

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
    public record CreationRequest (
        @NotNull UUID walletId,
        @NotNull UUID merchantId,
        @NotBlank String currency,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String orderId,
        @NotBlank String idempotencyKey
    ) {

    }

    @Schema(description = "결제 세션 생성 응답 - 소비자가 승인 전 확인할 예상 금액 포함")
    public record CreationResponse (
        UUID paymentId,
        String merchantName,
        BigDecimal amount,
        String currency,
        // TODO: 다통화 결제 구현 시 estimatedKrwAmount/exchangeRate/rateSource 응답 필드 확정
        String orderId,
        LocalDateTime createdAt
    ) {

    }

    @Schema(description = "결제 승인 요청 - MVP 범위에서는 별도의 body 없이 X-USER-ID와 paymentId로 승인 처리")
    public record ApproveRequest () { }

    @Schema(description = "결제 승인 응답")
    public record ApproveResponse (
        UUID paymentId,
        String status,
        BigDecimal amount,
        String currency,
        // TODO: 가맹점 정산 배치 구현 시 settledAmount / settledCurrency 응답 필드 확정
        // TODO: 다통화 결제 구현 시 exchangeRate/rateSource 응답 필드 확정
        LocalDateTime completedAt
    ) {

    }

    @Schema(description = "결제 취소 요청")
    public record CancelRequest (
    ) { }

    @Schema(description = "결제 취소 응답")
    public record CancelResponse(
            UUID paymentId,
            String status,
            LocalDateTime cancelledAt
    ) { }

    @Schema(description = "결제 상세 조회 응답")
    public record DetailViewResponse(
            UUID paymentId,
            String status,
            String merchantName,
            BigDecimal amount,
            String currency,
            // TODO: 가맹점 정산 배치 구현 시 settledAmount / settledCurrency 응답 필드 확정
            // TODO: 다통화 결제 구현 시 exchangeRate/rateSource 응답 필드 확정
            String orderId,
            LocalDateTime createdAt,
            LocalDateTime completedAt,
            LocalDateTime cancelledAt
    ) {

    }
}
