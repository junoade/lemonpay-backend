package com.lemonpay.member.domain;

import com.lemonpay.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    private Member(String email, String name, String phone) {
        this.email = validateEmail(email);
        this.name = validateName(name);
        this.phone = validatePhone(phone);
        this.status = MemberStatus.ACTIVE;
    }

    public static Member create(String email, String name, String phone) {
        return new Member(email, name, phone);
    }

    public void suspend() {
        throwsWhenMemberStatusClose();
        if (status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("활성화된 사용자만 이용 중지가 가능합니다.");
        }
        this.status = MemberStatus.SUSPENDED;
    }

    public void close() {
        throwsWhenMemberStatusClose();
        this.status = MemberStatus.CLOSED;
    }

    public void activate() {
        throwsWhenMemberStatusClose();
        if(status == MemberStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 활성화 된 사용자 입니다.");
        }
        this.status = MemberStatus.ACTIVE;
    }

    private String validateEmail(String email) {
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("email은 필수 입력값 입니다.");
        }
        return email.trim();
    }

    private String validateName(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수 입력값 입니다.");
        }
        return name.trim();
    }

    private String validatePhone(String phone) {
        if(phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone은 필수 입력값 입니다.");
        }

        String normalized = phone.trim();
        if(!normalized.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException("phone은 E.164 형식이어야 합니다.");
        }

        return normalized;
    }

    private void throwsWhenMemberStatusClose() {
        if(status == MemberStatus.CLOSED) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }
    }
}
