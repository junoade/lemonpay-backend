package com.lemonpay.exchange.interfaces;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.ExchangeCommand;
import com.lemonpay.exchange.application.ExchangeResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "환전 API 요청 및 응답 DTO")
public class ExchangeDto {

    @Schema(description = "기준 통화 금액을 지정하는 환전 요청")
    public record ExchangeRequest(
            @Schema(description = "환전할 지갑 ID")
            @NotNull UUID walletId,

            @Schema(description = "출금할 기준 통화", example = "USD")
            @NotNull Currency baseCurrency,

            @Schema(description = "입금할 대상 통화", example = "KRW")
            @NotNull Currency targetCurrency,

            @Schema(description = "기준 통화의 환전 요청 금액", example = "100.00")
            @NotNull @Positive BigDecimal baseAmount,

            @Schema(description = "환전 요청 식별 키 (1~100자)", example = "exchange-request-001")
            @NotBlank @Size(max = 100) String idempotencyKey
    ) {
        public ExchangeCommand toCommand() {
            return new ExchangeCommand(walletId, baseCurrency, targetCurrency, baseAmount, idempotencyKey);
        }
    }

    @Schema(description = "완료된 환전의 금액, 변경 후 잔액 및 적용 환율")
    public record ExchangeResponse(
            @Schema(description = "환전한 지갑 ID") UUID walletId,
            @Schema(description = "기준 통화", example = "USD") String baseCurrency,
            @Schema(description = "기준 통화 출금 금액", example = "100.00") BigDecimal baseAmount,
            @Schema(description = "대상 통화", example = "KRW") String targetCurrency,
            @Schema(description = "대상 통화 입금 금액", example = "135000") BigDecimal targetAmount,
            @Schema(description = "기준 통화 환전 후 잔액", example = "900.00") BigDecimal baseBalanceAfter,
            @Schema(description = "대상 통화 환전 후 잔액", example = "185000") BigDecimal targetBalanceAfter,
            @Schema(description = "기준 통화 1단위당 대상 통화의 적용 환율", example = "1350.00000000")
            BigDecimal appliedRate,
            @Schema(description = "적용 환율 기준일", example = "2026-10-13") LocalDate rateDate,
            @Schema(description = "적용 환율 고시 회차", example = "1") int roundNo,
            @Schema(description = "적용 환율 유형", example = "OFFICIAL") String rateType,
            @Schema(description = "적용 환율 출처", example = "API") String rateSource,
            @Schema(description = "환전 요청 식별 키", example = "exchange-request-001") String idempotencyKey
    ) {
        public static ExchangeResponse from(ExchangeResult result) {
            return new ExchangeResponse(
                    result.walletId(),
                    result.baseMoney().currency().getCode(),
                    result.baseMoney().amount(),
                    result.targetMoney().currency().getCode(),
                    result.targetMoney().amount(),
                    result.baseBalanceAfter().amount(),
                    result.targetBalanceAfter().amount(),
                    result.appliedRate(),
                    result.rateDate(),
                    result.roundNo(),
                    result.rateType().name(),
                    result.rateSource().name(),
                    result.idempotencyKey()
            );
        }
    }
}
