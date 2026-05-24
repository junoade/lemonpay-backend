package com.lemonpay.merchant.infrastructure;

import com.lemonpay.merchant.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantJpaRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByName(String name);
}
