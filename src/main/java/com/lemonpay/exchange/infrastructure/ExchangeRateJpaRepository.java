package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateJpaRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByBaseCurrencyAndTargetCurrency(
            Currency baseCurrency,
            Currency targetCurrency
    );
}
