package com.lemonpay.merchant.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface MerchantRepository {
    Merchant save(Merchant merchant);
    Optional<Merchant> findById(UUID id);
    List<Merchant> findAll();
    boolean existsById(UUID id);
}
