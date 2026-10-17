package com.lemonpay.exchange.infrastructure;

import com.lemonpay.exchange.domain.ExchangeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeTransactionJpaRepository extends JpaRepository<ExchangeTransaction, Long> {

}
