package com.lemonpay.exchange.domain;

import java.util.Optional;

public interface ExchangeTransactionRepository {

    ExchangeTransaction save(ExchangeTransaction exchangeTransaction);

    Optional<ExchangeTransaction> findById(Long id);

}
