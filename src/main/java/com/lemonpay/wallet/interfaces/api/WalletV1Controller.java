package com.lemonpay.wallet.interfaces.api;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.common.interfaces.ApiResponse;
import com.lemonpay.wallet.application.ChargeUseCase;
import com.lemonpay.wallet.application.WalletQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletV1Controller implements WalletV1ApiSpec {

    private final ChargeUseCase chargeUsecase;
    private final WalletQueryService walletQueryService;

    @Override
    public ResponseEntity<ApiResponse<WalletDto.ChargeResponse>> charge(UUID walletId, WalletDto.ChargeRequest request) {

        Currency currency = Currency.valueOf(request.currency());
        Money money = Money.of(request.amount(), currency);
        var result = chargeUsecase.charge(walletId, money);

        return ResponseEntity.ok(ApiResponse.of(WalletDto.ChargeResponse.from(result)));
    }

    @Override
    public ResponseEntity<ApiResponse<WalletDto.BalancesResponse>> getBalances(UUID walletId) {
        var result = walletQueryService.getWalletBalances(walletId);
        return ResponseEntity.ok(
                ApiResponse.of(WalletDto.BalancesResponse.from(result))
        );

    }

    @Override
    public ResponseEntity<ApiResponse<WalletDto.HistoryResponse>> getHistory(UUID walletId, String currency, Pageable pageable) {
        Currency currencyEnum = null;
        if (currency != null && !currency.isBlank()) {
            currencyEnum = Currency.valueOf(currency);
        }
        var resultPage = walletQueryService.queryLedgerEntries(walletId, currencyEnum, pageable);
        return ResponseEntity.ok(
                ApiResponse.of(WalletDto.HistoryResponse.from(walletId, resultPage))
        );
    }
}
