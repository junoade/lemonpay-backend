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

    public void validateWalletAccess(UUID walletId, UUID userId) {
        if(walletId == null || userId == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "입력값을 확인해주세요.");
        }

        if(!walletRepository.existsByIdAndMemberId(walletId, userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "지갑 소유자가 상이합니다.");
        }
    }
}
