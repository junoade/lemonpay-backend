package com.lemonpay.wallet.interfaces.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletV1Controller implements WalletV1ApiSpec {

    @Override
    public ResponseEntity<WalletDto.ChargeResponse> charge(UUID walletId, WalletDto.ChargeRequest request) {
        return ResponseEntity.ok(new WalletDto.ChargeResponse(walletId, "KRW", request.amount(), null));

    }

    @Override
    public ResponseEntity<WalletDto.BalancesResponse> getBalances(UUID walletId) {
        return ResponseEntity.ok(new WalletDto.BalancesResponse(walletId, List.of()));
    }
}
