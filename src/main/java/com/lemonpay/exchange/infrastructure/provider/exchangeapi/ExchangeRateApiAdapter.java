package com.lemonpay.exchange.infrastructure.provider.exchangeapi;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import com.lemonpay.exchange.application.port.outbound.ExchangeRateProvider;
import com.lemonpay.exchange.application.port.outbound.ExchangeRateProviderException;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "exchange.rate.provider",
        havingValue = "api"
)
public class ExchangeRateApiAdapter implements ExchangeRateProvider {

    private final RestClient exchangeRateRestClient;
    private final ExchangeRateApiProperties properties;

    @CircuitBreaker(name = "exchangeRateApi", fallbackMethod = "fallbackProviderFailure")
    @Override
    public ExchangeRateSnapshot fetch(Currency baseCurrency, Currency targetCurrency, LocalDate rateDate) {
        if (!properties.hasApiKey()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "환율 API Key가 설정되지 않았습니다.");
        }

        ExchangeRateApiResponse response = requestLatestRates(baseCurrency);
        validateSuccess(response);

        BigDecimal rate = response.rateOf(targetCurrency)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND,
                        "대상 통화 환율을 찾을 수 없습니다: %s/%s".formatted(baseCurrency, targetCurrency)
                ));

        return new ExchangeRateSnapshot(
                baseCurrency,
                targetCurrency,
                rate,
                rateDate,
                1,
                ExchangeRateType.OFFICIAL,
                ExchangeRateSource.API,
                LocalDateTime.now()
        );
    }

    private ExchangeRateApiResponse requestLatestRates(Currency baseCurrency) {
        try {
            return exchangeRateRestClient.get()
                    .uri("/v6/{apiKey}/latest/{baseCurrency}", properties.apiKey(), baseCurrency.name())
                    .retrieve()
                    .body(ExchangeRateApiResponse.class);
        } catch (RestClientException e) {
            throw new ExchangeRateProviderException("외부 환율 API 호출에 실패했습니다.");
        }
    }

    private void validateSuccess(ExchangeRateApiResponse response) {
        if (response == null) {
            throw new CoreException(ErrorType.INTERNAL_SERVER_ERROR, "환율 API 응답이 비어 있습니다.");
        }

        if (!response.isSuccess()) {
            throw new CoreException(
                    ErrorType.INTERNAL_SERVER_ERROR,
                    "환율 API 오류 응답: %s".formatted(response.errorType())
            );
        }
    }

    private ExchangeRateSnapshot fallbackProviderFailure(Currency baseCurrency,
                                                         Currency targetCurrency,
                                                         LocalDate rateDate,
                                                         Throwable throwable) {
        if(throwable instanceof CoreException e) {
            throw e;
        }

        if(throwable instanceof ExchangeRateProviderException e) {
            throw e;
        }
        // CallNotPermittedException 등의 예외를 provider 예외로 변환.
        throw new ExchangeRateProviderException("환율 Provider 호출에 실패했습니다 : ", throwable);
    }
}
