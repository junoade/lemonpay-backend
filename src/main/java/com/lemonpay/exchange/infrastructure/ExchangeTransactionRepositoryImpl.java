package com.lemonpay.exchange.infrastructure;

import com.lemonpay.exchange.domain.ExchangeTransaction;
import com.lemonpay.exchange.domain.ExchangeTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeTransactionRepositoryImpl implements ExchangeTransactionRepository {

    private final ExchangeTransactionJpaRepository jpaRepository;

    @Override
    public ExchangeTransaction save(ExchangeTransaction exchangeTransaction) {
        return jpaRepository.save(exchangeTransaction);
    }

    @Override
    public Optional<ExchangeTransaction> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
