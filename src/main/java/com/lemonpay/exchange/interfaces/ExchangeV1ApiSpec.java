package com.lemonpay.exchange.interfaces;

import com.lemonpay.common.interfaces.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Exchange V1 API", description = "지갑 내 두 통화 간 단건 환전")
public interface ExchangeV1ApiSpec {

    @PostMapping
    @Operation(
            summary = "단건 환전 실행",
            description = "저장된 방향별 최신 환율로 기준 통화 금액을 환전하고, "
                    + "두 통화의 환전 금액·변경 후 잔액·적용 환율을 반환합니다. "
                    + "지갑은 현재 사용자의 소유이며 ACTIVE 상태여야 합니다."
    )
    ResponseEntity<ApiResponse<ExchangeDto.ExchangeResponse>> exchange(
            @Valid @RequestBody ExchangeDto.ExchangeRequest request
    );
}
