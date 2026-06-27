package com.lemonpay.exchange.application;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.application.port.outbound.ExchangeRateProvider;
import com.lemonpay.exchange.domain.*;
import com.lemonpay.exchange.infrastructure.provider.exchangeapi.ExchangeRateApiProperties;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ActiveProfiles("local")
@SpringBootTest(properties = {
        "exchange.rate.provider=api",
        "exchange.rate.sync-ttl-minutes=60",
        "exchange.rate.scheduler.enabled=false"
})
@Transactional
public class ExchangeRateApiIntegrationTest {

    @Autowired
    private ExchangeRateSyncUseCase useCase;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateHistoryRepository exchangeRateHistoryRepository;
    @Autowired
    private ExchangeRateApiProperties exchangeRateApiProperties;

    @MockitoSpyBean
    private ExchangeRateProvider exchangeRateProvider;


    @Test
    @DisplayName("API key가 있으면 실제 API 환율을 조회해 history 내역과 master 원장에 저장한다.")
    void syncWithApiProvider_success() {
        // skip
        Assumptions.assumeTrue(exchangeRateApiProperties.hasApiKey(),
                "외부API_exchangerate-api.com 에 대한 API_KEY가 없어 통합테스트 스킵합니다.");

        // given & when
        ExchangeRateSnapshot snapshot = useCase.syncExchangeRate(Currency.USD, Currency.KRW);

        // then
        ExchangeRate master = exchangeRateRepository.findByCurrencyPair(Currency.USD, Currency.KRW)
                .orElseThrow();
        ExchangeRateHistory history = exchangeRateHistoryRepository.findLatestOfficial(Currency.USD, Currency.KRW)
                .orElseThrow();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.baseCurrency()).isEqualTo(Currency.USD);
        assertThat(snapshot.rate()).isPositive();
        assertThat(snapshot.source()).isEqualTo(ExchangeRateSource.API);
        assertThat(snapshot.rateType()).isEqualTo(ExchangeRateType.OFFICIAL);


        assertThat(master.getRate()).isEqualByComparingTo(snapshot.rate());
        assertThat(master.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(master.getSource()).isEqualTo(ExchangeRateSource.API);

        assertThat(history.getRate()).isEqualByComparingTo(snapshot.rate());
        assertThat(history.getRateType()).isEqualTo(ExchangeRateType.OFFICIAL);
        assertThat(history.getSource()).isEqualTo(ExchangeRateSource.API);
    }

    @Test
    @DisplayName("저장된 환율이 TTL 이내면 실제 API를 다시 호출하지 않고 동기화를 skip한다.")
    void syncIfStale_whenFreshRateExists_skipApiCall() {
        // skip
        Assumptions.assumeTrue(exchangeRateApiProperties.hasApiKey(),
                "외부API_exchangerate-api.com 에 대한 API_KEY가 없어 통합테스트 스킵합니다.");

        // given
        ExchangeRateSnapshot synced = useCase.syncExchangeRate(Currency.USD, Currency.KRW);
        reset(exchangeRateProvider);

        // when
        ExchangeRateSyncResult result = useCase.syncIfStale(Currency.USD, Currency.KRW);

        // then
        assertThat(result.status()).isEqualTo(ExchangeRateSyncStatus.SKIPPED);
        assertThat(result.snapshot().rate()).isEqualByComparingTo(synced.rate());
        verify(exchangeRateProvider, never()).fetch(any(), any(), any());
    }

}
