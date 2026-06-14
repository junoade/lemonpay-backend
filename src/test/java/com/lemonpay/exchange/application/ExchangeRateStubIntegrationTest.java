package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateHistory;
import com.lemonpay.exchange.domain.ExchangeRateHistoryRepository;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "exchange.rate.provider=stub"
})
@Transactional
public class ExchangeRateStubIntegrationTest {

    @Autowired
    private ExchangeRateSyncUseCase useCase;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;


    @Test
    @DisplayName("stub provider로 환율을 동기화하면 history 내역은 저장되고 master 원장은 upsert된다.")
    void syncWithStubProvider_success() {
        // given & when
        String expectedUsdToKrw = "1350.00000000";
        ExchangeRateSnapshot snapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        assertThat(snapshot.rate()).isEqualByComparingTo(expectedUsdToKrw);
        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        assertThat(master.getRate()).isEqualByComparingTo(expectedUsdToKrw);

        ExchangeRateHistory history = exchangeRateHistoryRepository.findLatestOfficial(Currency.USD, Currency.KRW)
                .orElseThrow();

        assertThat(history.getRate()).isEqualByComparingTo(expectedUsdToKrw);
    }


}
