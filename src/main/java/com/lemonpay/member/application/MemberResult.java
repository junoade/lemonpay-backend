package com.lemonpay.member.application;

import com.lemonpay.member.domain.Member;
import com.lemonpay.wallet.domain.Wallet;

import java.time.LocalDateTime;
import java.util.UUID;

public class MemberResult {
    public record CommonResult(
            UUID memberId,
            String email,
            String name,
            String phone,
            String status,
            UUID walletId,
            String walletName,
            String walletProductCode,
            LocalDateTime createdAt,
            LocalDateTime updateAt
    ) {
        public static CommonResult from(Member member, Wallet wallet) {
            return new CommonResult(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getPhone(),
                    member.getStatus().name(),
                    wallet.getId(),
                    wallet.getName(),
                    wallet.getProductCode(),
                    member.getCreatedAt(),
                    member.getUpdatedAt()
            );
        }
    }


}
