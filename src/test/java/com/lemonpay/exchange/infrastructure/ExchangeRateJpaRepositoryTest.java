package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DataJpaTest
class ExchangeRateJpaRepositoryTest {

    private static final LocalDate RATE_DATE = LocalDate.of(2026, 6, 9);
    private static final LocalDateTime FETCHED_AT = LocalDateTime.of(2026, 6, 9, 9, 0);

    @Autowired
    private ExchangeRateJpaRepository exchangeRateJpaRepository;

    @Test
    @DisplayName("id 없는 새 엔티티를 같은 통화쌍으로 save하면 INSERT가 발생해 unique 제약에 걸린다.")
    void saveNewEntityWithSameCurrencyPair_throwsUniqueConstraintException() {
        // given
        exchangeRateJpaRepository.saveAndFlush(createUsdKrwRate("1350.00000000"));
        ExchangeRate duplicatedCurrencyPair = createUsdKrwRate("1360.00000000");

        // when & then
        assertThatThrownBy(() -> exchangeRateJpaRepository.saveAndFlush(duplicatedCurrencyPair))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("조회한 기존 엔티티를 변경한 뒤 save하면 같은 row가 UPDATE된다.")
    void saveManagedEntity_updatesExistingRow() {
        // given
        exchangeRateJpaRepository.saveAndFlush(createUsdKrwRate("1350.00000000"));
        ExchangeRate found = exchangeRateJpaRepository
                .findByBaseCurrencyAndTargetCurrency(Currency.USD, Currency.KRW)
                .orElseThrow();

        // when
        found.update(
                new BigDecimal("1360.00000000"),
                RATE_DATE.plusDays(1),
                2,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                FETCHED_AT.plusDays(1)
        );
        exchangeRateJpaRepository.saveAndFlush(found);

        // then
        ExchangeRate updated = exchangeRateJpaRepository
                .findByBaseCurrencyAndTargetCurrency(Currency.USD, Currency.KRW)
                .orElseThrow();

        assertThat(exchangeRateJpaRepository.count()).isEqualTo(1);
        assertThat(updated.getRate()).isEqualByComparingTo("1360.00000000");
        assertThat(updated.getRateDate()).isEqualTo(RATE_DATE.plusDays(1));
        assertThat(updated.getRoundNo()).isEqualTo(2);
    }

    private ExchangeRate createUsdKrwRate(String rate) {
        return ExchangeRate.create(
                Currency.USD,
                Currency.KRW,
                new BigDecimal(rate),
                RATE_DATE,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                FETCHED_AT
        );
    }

}
