package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {


    @Nested
    @DisplayName("지갑 생성 테스트")
    public class Create {
        Wallet wallet;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
        }

        @Test
        @DisplayName("지갑 생성시 초기 상태는 ACTIVE 이다.")
        void create_thenActive() {
            assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
        }

        @Test
        @DisplayName("지갑 생성 시 기본 통화(KRW, USD, JPY)와 초기 잔액 0이 설정된다.")
        void create_thenDefaultCurrency() {
            List<WalletBalance> balances = wallet.getBalances();

            assertAll(
                    () -> assertThat(balances).hasSize(3),
                    () -> assertThat(balances)
                            .extracting(WalletBalance::getCurrency)
                            .containsExactlyInAnyOrder(
                                    Currency.KRW,
                                    Currency.USD,
                                    Currency.JPY
                            ),
                    () -> assertThat(balances)
                            .allSatisfy(balance ->
                                    assertThat(balance.toMoney()).isEqualTo(Money.zero(balance.getCurrency()))
                            )
            );
        }
    }

    @Nested
    @DisplayName("지갑 조회 테스트")
    public class Select {
        Wallet wallet;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
        }

        @Test
        @DisplayName("미지원 통화 잔액 조회시 예외가 발생한다.")
        void selectUnsupportedCurrency_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> wallet.getBalance(Currency.valueOf("EUR")));
        }

    }


    @Nested
    @DisplayName("지갑 상태 전이 테스트")
    public class Transition {

        Wallet wallet;

        @BeforeEach
        void init() {
            wallet = createTestWallet();
        }

        @Test
        @DisplayName("ACTIVE 상태에서 freeze 하면 FROZEN 된다.")
        void activeToFrozen_thenSuccess() {

            wallet.freeze();

            assertEquals(WalletStatus.FROZEN, wallet.getStatus());
        }

        @Test
        @DisplayName("ACTIVE 사태에서 close 하면 CLOSE 된다.")
        void activeToClose_thenSuccess() {
            wallet.close();
            assertEquals(WalletStatus.CLOSED, wallet.getStatus());
        }

        @Test
        @DisplayName("FROZEN 상태에서 close 하면 CLOSE 된다.")
        void frozenToClose_thenSuccess() {
            wallet.freeze();
            wallet.close();
            assertEquals(WalletStatus.CLOSED, wallet.getStatus());
        }

        @Test
        @DisplayName("FROZEN 상태에서 close 하면 CLOSE 된다.")
        void frozenToActive_thenSuccess() {
            wallet.freeze();
            wallet.activate();
            assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
        }

        @Test
        @DisplayName("ACTIVE 상태에서 다시 active로 하려고 하면 예외가 발생한다.")
        void activeToActive_throwsException() {
            assertThrows(IllegalStateException.class, () -> wallet.activate());
        }

        @Test
        @DisplayName("FROZEN 상태에서 다시 freeze 하려고 하면 예외가 발생한다.")
        void frozenToFrozen_throwsException() {
            wallet.freeze();
            assertThrows(IllegalStateException.class, () -> wallet.freeze());
        }


        @Test
        @DisplayName("CLOSED 된 상태에서 상태 전이시 예외가 발생한다.")
        void closeToSomething_throwsException() {
            wallet.close();

            assertAll(
                    () ->assertThrows(IllegalStateException.class, () -> wallet.freeze()),
                    () -> assertThrows(IllegalStateException.class, () -> wallet.close()),
                    () -> assertThrows(IllegalStateException.class, () -> wallet.activate())
            );
        }


    }

    private Member createTestMember() {
        return Member.create("test@gmail.com", "JunhoTest", "+821012341234");
    }

    private Wallet createTestWallet() {
        Member member = createTestMember();
        return Wallet.create(member, "TestWallet", "BASC-V01");
    }
}