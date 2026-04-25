package com.lemonpay.wallet.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Wallet V1 API")
public interface WalletV1ApiSpec {

    @PostMapping("/{walletId}/charge")
    @Operation(
            summary = "지갑 충전",
            description = "지갑 충전을 통해 지갑 잔액을 증가 시킵니다."
    )
    ResponseEntity<WalletDto.ChargeResponse> charge(
            @Parameter(description = "지갑 ID", required = true)
            @PathVariable("walletId") UUID walletId,
            @Valid @RequestBody WalletDto.ChargeRequest request);

    @GetMapping("/{walletId}/balances")
    @Operation(
            summary = "잔액 조회",
            description = "지갑의 현재 잔액을 조회합니다."
    )
    ResponseEntity<WalletDto.BalancesResponse> getBalances(
            @Parameter(description = "지갑 ID", required = true)
            @PathVariable("walletId") UUID walletId);

    @GetMapping("/{walletId}/history")
    @Operation(
            summary = "거래 내역 조회",
            description = "지갑의 거래 내역을 최신순으로 페이징 조회합니다. currency 파라미터가 없으면 전체 통화를 반환합니다."
    )
    ResponseEntity<WalletDto.HistoryResponse> getHistory(
            @Parameter(description = "지갑 ID", required = true)
            @PathVariable("walletId") UUID walletId,

            @Parameter(description = "통화 필터 (선택)", example = "KRW")
            @RequestParam(value = "currency", required = false) String currency,

            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);
}
