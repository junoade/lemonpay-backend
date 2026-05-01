package com.lemonpay.merchant.infrastructure;

import com.lemonpay.merchant.domain.Merchant;
import com.lemonpay.merchant.domain.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MerchantRepositoryImpl implements MerchantRepository {

    private final MerchantJpaRepository jpaRepository;

    @Override
    public Merchant save(Merchant merchant) {
        return jpaRepository.save(merchant);
    }

    @Override
    public Optional<Merchant> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
