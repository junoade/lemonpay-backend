package com.lemonpay.exchange.interfaces;

import com.lemonpay.common.interfaces.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Tag(name = "ExchangeRate V1 API Specification")
public interface ExchangeRateV1ApiSpec {

    @GetMapping(value = "/latest", params = {"baseCurrency", "targetCurrency"})
    @Operation(
            summary = "Base/Target 환율 정보 단건 조회 API",
            description = "Base/Target 환율 정보를 단건 조회 후 응답합니다."
    )
    ResponseEntity<ApiResponse<ExchangeRateDto.RateResponse>> getLatestCurrencyRate(
            @Valid @ModelAttribute ExchangeRateDto.RateRequest request
    );

    @GetMapping
    @Operation(
            summary = "Base/Target 환율 정보 다건 조회 API",
            description = "현재 제공하는 Base/Target 환율 정보 목록을 조회하여 다건 응답합니다."
    )
    ResponseEntity<ApiResponse<List<ExchangeRateDto.RateResponse>>> getLatestCurrencyRates();
}
