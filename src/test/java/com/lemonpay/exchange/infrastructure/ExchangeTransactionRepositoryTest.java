package com.lemonpay.exchange.infrastructure;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.domain.Money;
import com.lemonpay.exchange.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ExchangeTransactionRepositoryImpl.class)
class ExchangeTransactionRepositoryTest {

    @Autowired
    private ExchangeTransactionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("KRW/USD 환전 거래를 저장하고 다시 조회해도 적용 환율의 소수점 8자리가 보존된다.")
    void preservesRate() {
        // given
        ExchangeRate rate = ExchangeRate.create(
                Currency.KRW, Currency.USD, new BigDecimal("0.00074074"),
                LocalDate.of(2026, 10, 10), 1,
                ExchangeRateType.OFFICIAL, ExchangeRateSource.API,
                LocalDateTime.of(2026, 10, 10, 10, 0)
        );
        ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.won(135_000), rate);
        ExchangeTransaction transaction = ExchangeTransaction.completed(
                UUID.randomUUID(), "exchange-rate-001", quote, rate,
                Money.won(65_000), Money.usd("100.00")
        );

        // when
        Long id = repository.save(transaction).getId();
        entityManager.flush(); // 변경 내용을 DB에 반영

        entityManager.clear(); // 영속성 컨텍스트를 비운 후 DB에서 다시조회
        ExchangeTransaction found = repository.findById(id).orElseThrow();

        // then
        assertThat(found.getAppliedRate()).isEqualByComparingTo("0.00074074");
        assertThat(found.getAppliedRate().scale()).isEqualTo(8);
    }

    @Test
    @DisplayName("USD/KRW 환전 거래를 저장하면 식별자가 반환되고 거래 정보와 환전 후 잔액이 보존된다.")
    void savesTransaction() {
        // given
        UUID walletId = UUID.randomUUID();
        String key = "exchange-save-001";
        LocalDate rateDate = LocalDate.of(2026, 10, 10);
        ExchangeRate rate = ExchangeRate.create(
                Currency.USD, Currency.KRW, new BigDecimal("1350.00000000"),
                rateDate, 2,
                ExchangeRateType.OFFICIAL, ExchangeRateSource.API,
                LocalDateTime.of(2026, 10, 10, 10, 0)
        );
        ExchangeQuote quote = ExchangeQuote.fromBaseAmount(Money.usd("100.00"), rate);
        ExchangeTransaction transaction = ExchangeTransaction.completed(
                walletId, key, quote, rate,
                Money.usd("75.25"), Money.won(145_000)
        );

        // when
        ExchangeTransaction saved = repository.save(transaction);
        assertThat(saved.getId()).isNotNull();
        Long id = saved.getId();

        entityManager.flush();

        entityManager.clear();
        ExchangeTransaction found = repository.findById(id).orElseThrow();

        // then
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getWalletId()).isEqualTo(walletId);
        assertThat(found.getIdempotencyKey()).isEqualTo(key);
        assertThat(found.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(found.getBaseAmount()).isEqualByComparingTo("100.00");
        assertThat(found.getTargetCurrency()).isEqualTo(Currency.KRW);
        assertThat(found.getTargetAmount()).isEqualByComparingTo("135000");
        assertThat(found.getBaseBalanceAfter()).isEqualByComparingTo("75.25");
        assertThat(found.getTargetBalanceAfter()).isEqualByComparingTo("145000");
        assertThat(found.getAppliedRate()).isEqualByComparingTo("1350.00000000");
        assertThat(found.getRateDate()).isEqualTo(rateDate);
        assertThat(found.getRoundNo()).isEqualTo(2);
        assertThat(found.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(found.getRateSource()).isEqualTo(ExchangeRateSource.API);
    }
}
