package com.lemonpay.merchant.domain;

import com.lemonpay.common.exception.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MerchantTest {

    @Nested
    @DisplayName("가맹점 생성 테스트")
    class Create {

        @DisplayName("최초 가맹점 생성시, active 상태로 가맹점이 등록된다.")
        void createMerchant_success() {
            // given
            String name = "Test Merchant";
            String callbackUrl = "https://localhost:8080/testUrl";
            // when
            Merchant merchant = Merchant.create(name, callbackUrl);

            // then
            assertNotNull(merchant);
            assertEquals(name, merchant.getName());
            assertEquals(callbackUrl, merchant.getCallbackUrl());
            assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        }
    }

    @Nested
    @DisplayName("가맹점 상태 변경 테스트")
    class Update {

        @Test
        @DisplayName("가맹점 상태가 supseneded 면 레몬페이 사용자는 가맹점으로 결제를 진행할 수 없다.")
        void validatePayable_throwsException() {
            // given
            String name = "Test Merchant";
            String callbackUrl = "https://localhost:8080/testUrl";
            Merchant merchant = Merchant.create(name, callbackUrl);
            merchant.suspend();

            // when & then
            assertThrows(CoreException.class, merchant::validatePayable);
        }
    }
}