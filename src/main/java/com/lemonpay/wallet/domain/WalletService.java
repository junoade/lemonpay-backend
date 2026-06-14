package com.lemonpay.wallet.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new CoreException(ErrorType.WALLET_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Wallet getWalletByMemberId(UUID memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CoreException(ErrorType.WALLET_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public void validateWalletAccess(UUID walletId, UUID userId) {
        if(walletId == null || userId == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "입력값을 확인해주세요.");
        }

        if(!walletRepository.existsByIdAndMemberId(walletId, userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "지갑 소유자가 상이합니다.");
        }
    }

    @Transactional
    public Wallet createDefaultWallet(Member member, String name, String productCode) {
        if(walletRepository.existsByMemberId(member.getId())) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "현재는 1인당 1개의 지갑만 소유 가능합니다.");
        }
        return walletRepository.save(Wallet.create(member, name, productCode));
    }

}
