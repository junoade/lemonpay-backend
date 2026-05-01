package com.lemonpay.merchant.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "merchants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Merchant extends BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantStatus status;

    @Column(nullable = false, length = 5000)
    private String callbackUrl;

    private Merchant(String name, MerchantStatus status, String callbackUrl) {
        this.name = name;
        this.status = status;
        this.callbackUrl = callbackUrl;
    }

    public static Merchant create(String name, String callbackUrl) {
        return new Merchant(name, MerchantStatus.ACTIVE , callbackUrl);
    }

    public void suspend() {
        this.status.validateTransition(MerchantStatus.SUSPENDED);
        this.status = MerchantStatus.SUSPENDED;
    }

    public void close() {
        this.status.validateTransition(MerchantStatus.CLOSED);
        this.status = MerchantStatus.CLOSED;
    }

    public void activate() {
        this.status.validateTransition(MerchantStatus.ACTIVE);
        this.status = MerchantStatus.ACTIVE;
    }

    public void validatePayable() {
        if(status != MerchantStatus.ACTIVE) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "가맹점이 결제 요청 보낼 수 없는 상태입니다.");
        }
    }

}
