package com.lemonpay.wallet.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new CoreException(ErrorType.WALLET_NOT_FOUND));
    }
}
