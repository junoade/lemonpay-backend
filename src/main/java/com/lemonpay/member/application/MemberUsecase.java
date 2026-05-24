package com.lemonpay.member.application;

import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberService;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberUsecase {

    private final MemberService memberService;
    private final WalletService walletService;

    @Transactional
    public MemberResult.CommonResult join(MemberCommand.Join join) {
        Member member = memberService.createMember(join.email(), join.name(), join.phone());
        Wallet wallet = walletService.createDefaultWallet(member, join.walletName(), "BASC-V1");
        return MemberResult.CommonResult.from(member, wallet);
    }

    @Transactional(readOnly = true)
    public MemberResult.CommonResult login(String email, String name) {
        Member member = memberService.validateAccess(email, name);
        Wallet wallet = walletService.getWalletByMemberId(member.getId());
        return MemberResult.CommonResult.from(member, wallet);
    }
}
