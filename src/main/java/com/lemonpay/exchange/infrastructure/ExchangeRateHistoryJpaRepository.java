package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateHistoryJpaRepository extends JpaRepository<ExchangeRateHistory, Long> {

    Optional<ExchangeRateHistory> findFirstByBaseCurrencyAndTargetCurrencyAndRateTypeOrderByRateDateDescRoundNoDesc(
            Currency baseCurrency,
            Currency targetCurrency,
            ExchangeRateType rateType
    );

    Optional<ExchangeRateHistory> findFirstByBaseCurrencyAndTargetCurrencyAndRateDateOrderByRoundNoDesc(
            Currency baseCurrency,
            Currency targetCurrency,
            LocalDate rateDate
    );
}
