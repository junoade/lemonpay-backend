package com.lemonpay.exchange.interfaces;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.common.interfaces.ApiResponse;
import com.lemonpay.exchange.application.ExchangeRateQueryService;
import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateV1Controller implements ExchangeRateV1ApiSpec {

    private final ExchangeRateQueryService queryService;

    @Override
    public ResponseEntity<ApiResponse<ExchangeRateDto.RateResponse>> getLatestCurrencyRate(
            ExchangeRateDto.RateRequest request) {
        Currency baseCurrency = Currency.valueOf(request.baseCurrency().toUpperCase());
        Currency targetCurrency = Currency.valueOf(request.targetCurrency().toUpperCase());
        ExchangeRateSnapshot result = queryService.getLatestExchangeRate(baseCurrency, targetCurrency);
        return ResponseEntity.ok(
                ApiResponse.of(ExchangeRateDto.RateResponse.from(result))
        );
    }

    @Override
    public ResponseEntity<ApiResponse<List<ExchangeRateDto.RateResponse>>> getLatestCurrencyRates() {
        List<ExchangeRateSnapshot> list = queryService.getLatestExchangeRates();
        return ResponseEntity.ok(
                ApiResponse.of(ExchangeRateDto.RateResponse.from(list))
        );
    }

}
