package com.lemonpay.wallet.interfaces.api;

import com.lemonpay.common.domain.Money;
import com.lemonpay.wallet.application.ChargeResult;
import com.lemonpay.wallet.application.WalletQueryResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "지갑 충전/잔액 조회 등 API 요청 및 응답에 대한 DTO")
public class WalletDto {

    @Schema(description = "지갑 충전 요청 DTO")
    public record ChargeRequest(
            // TODO: 인증 구현 후 memberId는 JWT에서 추출, walletId는 PathVariable로만 받도록 변경
            @Schema(description = "회원 ID", example = "650e8400-e29b-41d4-a716-446655440000")
            @NotNull UUID memberId,

            @Schema(description = "통화코드", example = "KRW")
            @NotBlank String currency,

            @Schema(description = "금액", example = "1000")
            @NotNull @Positive BigDecimal amount
    ) { }

    @Schema(description = "지갑 충전 응답 DTO")
    public record ChargeResponse(
            @Schema(description = "지갑 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID walletId,

            @Schema(description = "충전 통화", example = "KRW")
            String currency,

            @Schema(description = "충전 금액", example = "1000.0000")
            BigDecimal chargedAmount,

            @Schema(description = "충전 후 잔액 정보")
            WalletBalanceItem balance
    ) {
        public static ChargeResponse from(ChargeResult chargeResult) {
            return new ChargeResponse(
                    chargeResult.walletId(),
                    chargeResult.chargeMoney().currency().toString(),
                    chargeResult.chargeMoney().amount(),
                    WalletBalanceItem.from(chargeResult.afterBalance())
            );
        }

    }

    @Schema(description = "잔액 조회 응답 DTO")
    public record BalancesResponse(
            @Schema(description = "지갑 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID walletId,

            @Schema(description = "현재 다통화 잔액 정보")
            List<WalletBalanceItem> balances
    ) {
        public static BalancesResponse from(WalletQueryResult walletQueryResult) {
            var balances = walletQueryResult.walletBalances()
                    .stream().map(WalletBalanceItem::from)
                    .toList();

            return new BalancesResponse(
                    walletQueryResult.walletId(),
                    balances
            );
        }
    }

    @Schema(description = "지갑 내 단일 통화 잔액 정보")
    public record WalletBalanceItem(
            String currency,
            BigDecimal amount
    ) {
        public static WalletBalanceItem from(Money money) {
            return new WalletBalanceItem(
                    money.currency().toString(),
                    money.amount()
            );
        }
    }

}
