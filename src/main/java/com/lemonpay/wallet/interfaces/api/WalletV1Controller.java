package com.lemonpay.wallet.interfaces.api;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.wallet.application.ChargeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletV1Controller implements WalletV1ApiSpec {

    private final ChargeUseCase chargeUsecase;

    @Override
    public ResponseEntity<WalletDto.ChargeResponse> charge(UUID walletId, WalletDto.ChargeRequest request) {

        Currency currency = Currency.valueOf(request.currency());
        Money money = Money.of(request.amount(), currency);
        var result = chargeUsecase.charge(walletId, currency, money);

        return ResponseEntity.ok(WalletDto.ChargeResponse.from(result));
    }

    @Override
    public ResponseEntity<WalletDto.BalancesResponse> getBalances(UUID walletId) {
        return ResponseEntity.ok(new WalletDto.BalancesResponse(walletId, List.of()));
    }
}
