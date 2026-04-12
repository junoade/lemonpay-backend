package com.lemonpay.common.config;

import com.lemonpay.common.domain.Money;
import com.lemonpay.ledger.domain.Direction;
import com.lemonpay.ledger.domain.EntryType;
import com.lemonpay.ledger.domain.LedgerEntry;
import com.lemonpay.ledger.domain.LedgerEntryRepository;
import com.lemonpay.member.domain.Member;
import com.lemonpay.member.domain.MemberRepository;
import com.lemonpay.wallet.domain.Wallet;
import com.lemonpay.wallet.domain.WalletBalance;
import com.lemonpay.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalDataInitializerConfig {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Bean
    public ApplicationRunner localDataInitializer() {
        return args -> seed();
    }

    /**
     * local 환경에서의 초기 데이터 생성을 수행합니다.
     * <p>생성 흐름
     * - Member 데이터 생성 -> Wallet 데이터 생성 -> 기본 통화(KRW/USD/JPY) 에 대한 WalletBalance 생성
     * </p>
     */
    @Transactional
    public void seed() {
        log.info("Profile : local, Run local data initializer.");
        for(int i = 1; i <= 5; i++) {
            String suffix = String.format("%04d", i);
            String email = "test" + suffix + "@lemonpay.com";
            String name = "test" + suffix;
            String phone = "test" + suffix;

            Member member = Member.create(email, name,phone);
            memberRepository.save(member);

            Wallet wallet = Wallet.create(member, "기본 지갑", "BASC-V1");
            wallet.initDefaultCurrency();
            walletRepository.save(wallet);

            charge(wallet, Money.won(50_000));
            charge(wallet, Money.usd("1000.00"));
            charge(wallet, Money.jpy(1_000));
        }
    }

    private void charge(Wallet wallet, Money amount) {
        WalletBalance balance = wallet.getBalance(amount.currency());
        balance.increase(amount);

        LedgerEntry ledgerEntry = LedgerEntry.of(
                wallet.getId(),
                amount,
                balance.toMoney(),
                EntryType.CHARGE
        );

        ledgerEntryRepository.save(ledgerEntry);
    }
}
