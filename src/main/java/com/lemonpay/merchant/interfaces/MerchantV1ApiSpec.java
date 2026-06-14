package com.lemonpay.merchant.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Merchant V1 API Specification")
public interface MerchantV1ApiSpec {

    @GetMapping
    @Operation(
            summary = "가맹점 목록 조회 API",
            description = "등록된 가맹점 목록을 조회합니다."
    )
    ResponseEntity<MerchantDto.MerchantListResponse> getMerchants();

    @PostMapping
    @Operation(
            summary = "가맹점 생성 API",
            description = "신규 가맹점을 등록합니다."
    )
    ResponseEntity<MerchantDto.MerchantResponse> create(
            @Valid @RequestBody MerchantDto.CreationRequest request
    );

    @GetMapping("/{merchantId}")
    @Operation(
            summary = "가맹점 단건 조회 API",
            description = "가맹점 ID로 가맹점 상세 정보를 조회합니다."
    )
    ResponseEntity<MerchantDto.MerchantResponse> getMerchant(
            @Parameter(description = "가맹점 ID", required = true)
            @PathVariable("merchantId") UUID merchantId
    );
}
