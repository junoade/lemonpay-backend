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

    // TODO: E.164 형식 (+821012345678)
    @Column(nullable = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    private Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.status = MemberStatus.ACTIVE;
    }

    public static Member create(String email, String name, String phone) {
        return new Member(email, name, phone);
    }

    public void suspend() {
        if (status != MemberStatus.ACTIVE) {
            throw new IllegalStateException("활성화된 사용자만 이용 중지가 가능합니다.");
        }
        this.status = MemberStatus.SUSPENDED;
    }

    public void close() {
        if (status == MemberStatus.CLOSED) {
            throw new IllegalStateException("이미 탈퇴된 회원입니다.");
        }
        this.status = MemberStatus.CLOSED;
    }
}
