package com.lemonpay.member.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    @Nested
    @DisplayName("생성 테스트")
    class Create {

        @Test
        @DisplayName("회원 생성 시 전화번호 포맷은 E.164 형식을 따라야하고 그렇지 않으면 예외가 발생한다.")
        void create_givenWrongPhone_throwsException() {
            String email = "test@gmail.com";
            String name = "test";
            String phone = "01012341234";
            assertThrows(IllegalArgumentException.class, () -> Member.create(email, name, phone));
        }

        @Test
        @DisplayName("회원 생성 시 전화번호 포맷은 E.164 형식을 따라야하며 그럴 경우 정상적으로 생성된다.")
        void create_givenRightPhone() {
            String email = "test@gmail.com";
            String name = "test";
            String phone = "+821012341234";
            Member member = Member.create(email, name, phone);

            assertAll(
                    () -> assertThat(member.getEmail()).isEqualTo(email),
                    () -> assertThat(member.getPhone()).isEqualTo(phone),
                    () -> assertThat(member.getName()).isEqualTo(name),
                    () -> assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE)
            );
        }

        @Test
        @DisplayName("회원 생성 시 입력값은 trim 되어 저장된다.")
        void create_trim() {
            String email = " test@gmail.com ";
            String name = " test ";
            String phone = "+821012341234";
            Member member = Member.create(email, name, phone);

            assertAll(
                    () -> assertThat(member.getEmail()).isEqualTo("test@gmail.com"),
                    () -> assertThat(member.getName()).isEqualTo("test")
            );
        }

        @Test
        @DisplayName("null 값 입력시 도메인 검증 로직으로 IllegalArgumentException 예외가 반환된다.")
        void create_null() {
            assertThrows(IllegalArgumentException.class, () -> Member.create(null, null, null));
        }

    }

    @Nested
    @DisplayName("변경 테스트")
    class Update {

        private Member member;

        @BeforeEach
        void init() {
            member = Member.create("test@gmail.com", "test", "+821012341234");
        }

        @Test
        @DisplayName("이용 중인 상태에서 이용 중지 상태로 전이가 된다.")
        void suspend() {
            member.suspend();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.SUSPENDED);
        }

        @Test
        @DisplayName("탈퇴된 회원을 이용 중지하려고 하면 IllegalStateException 예외가 발생한다.")
        void suspend_givenCloseStatus_throwsException() {
            member.close();
            assertThrows(IllegalStateException.class, () -> member.suspend());
        }

        @Test
        @DisplayName("이미 이용 중지된 회원의 상태를 이용 중지로 변경하려고 하면 IllegalStateException 예외가 발생한다.")
        void suspend_givenAlreadySuspended_throwsException() {
            member.suspend();
            assertThrows(IllegalStateException.class, () -> member.suspend());
        }

        @Test
        @DisplayName("활성화된 사용자를 탈퇴할 수 있다.")
        void close() {
            member.close();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.CLOSED);
        }

        @Test
        @DisplayName("활성화된 사용자를 탈퇴할 수 있다.")
        void close_givenAlreadyClosedStatus_throwsException() {
            member.close();
            assertThrows(IllegalStateException.class, () -> member.close());
        }

        @Test
        @DisplayName("이용 중지된 사용자도 회원 탈퇴할 수 있다.")
        void close_givenSuspended() {
            member.suspend();
            member.close();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.CLOSED);
        }

        @Test
        @DisplayName("이용 중지된 사용자는 다시 활성화 될 수 있다.")
        void activate_givenSuspended() {
            member.suspend();
            member.activate();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }
    }
}