package com.lemonpay.exchange.interfaces;

import com.lemonpay.common.interfaces.ApiResponse;
import com.lemonpay.exchange.application.ExchangeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeV1Controller implements ExchangeV1ApiSpec {

    private final ExchangeUseCase exchangeUseCase;

    @Override
    public ResponseEntity<ApiResponse<ExchangeDto.ExchangeResponse>> exchange(ExchangeDto.ExchangeRequest request) {
        var result = exchangeUseCase.exchange(request.toCommand());
        return ResponseEntity.ok(ApiResponse.of(ExchangeDto.ExchangeResponse.from(result)));
    }
}
