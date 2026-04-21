package com.lemonpay.wallet.interfaces.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletV1Controller implements WalletV1ApiSpec {

    @Override
    public ResponseEntity<Void> charge(UUID walletId, Map<String, Object> requestBody) {
        return null;
    }

    @Override
    public ResponseEntity<?> getBalances(UUID walletId) {
        return null;
    }
}
