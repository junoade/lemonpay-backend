package com.lemonpay.merchant.domain;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository {
    Merchant save(Merchant merchant);
    Optional<Merchant> findById(UUID id);
    boolean existsById(UUID id);
}
