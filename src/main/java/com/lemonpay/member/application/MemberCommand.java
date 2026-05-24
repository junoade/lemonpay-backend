package com.lemonpay.member.application;

public class MemberCommand {
    public record Join(
            String email,
            String name,
            String phone,
            String walletName,
            String productCode
    ){ }
}
