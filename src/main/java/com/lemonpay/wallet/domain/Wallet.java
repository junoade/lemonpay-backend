package com.lemonpay.wallet.domain;

import com.lemonpay.common.domain.BaseEntity;
import com.lemonpay.common.domain.Currency;
import com.lemonpay.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wallet")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletStatus status;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletBalance> balances = new ArrayList<>();

    private Wallet(Member member, String name, String productCode) {
        this.member = member;
        this.name = name;
        this.productCode = productCode;
        this.isPrimary = false;
        this.status = WalletStatus.ACTIVE;
    }
    public static Wallet create(Member member, String name, String productCode) {
        return new Wallet(member, name, productCode);
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public void freeze() {
        if (this.status == WalletStatus.FROZEN) {
            throw new IllegalStateException("이미 동결된 지갑입니다.");
        }
        this.status = WalletStatus.FROZEN;
    }

    public void close() {
        if (this.status == WalletStatus.CLOSED) {
            throw new IllegalStateException("이미 해지된 지갑입니다.");
        }
        this.status = WalletStatus.CLOSED;
    }

    public void initDefaultCurrency() {
        addBalance(WalletBalance.zero(this, Currency.KRW));
        addBalance(WalletBalance.zero(this, Currency.USD));
        addBalance(WalletBalance.zero(this, Currency.JPY));
    }

    private void addBalance(WalletBalance walletBalance) {
        this.balances.add(walletBalance);
    }

    public WalletBalance getBalance(Currency currency) {
        return balances.stream()
                .filter(b -> b.getCurrency() == currency)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("미지원 통화이거나 현재 지갑에 해당 통화 잔액이 없습니다. 현재 통화" + currency));
    }
}
