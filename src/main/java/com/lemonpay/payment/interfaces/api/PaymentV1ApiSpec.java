package com.lemonpay.payment.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment V1 API Specification")
public interface PaymentV1ApiSpec {

    @PostMapping
    @Operation(
            summary = "가맹점 결제 요청 생성 API",
            description = "가맹점 클라이언트에서 레몬페이로 결제 요청 생성"
    )
    ResponseEntity<PaymentDto.PaymentResponse> create(
            @Valid @RequestBody PaymentDto.CreationRequest request
    );

    @PostMapping("/{txNo}/approve")
    @Operation(
            summary = "결제 승인 API",
            description = "레몬페이 잔액 차감 및 결제 승인을 처리"
    )
    ResponseEntity<PaymentDto.PaymentResponse> approve(
            @Parameter(description = "결제 ID", required = true)
            @PathVariable("txNo") String txNo
    );

    @PostMapping("/{txNo}/cancel")
    @Operation(
            summary = "결제 취소 API",
            description = "레몬페이 결제 취소 처리"
    )
    ResponseEntity<PaymentDto.PaymentResponse> cancel(
            @Parameter(description = "결제 ID", required = true)
            @PathVariable("txNo") String txNo
    );

    @GetMapping("/{txNo}")
    @Operation(
            summary = "결제 내역 조회 API",
            description = "결제 ID에 대한 세부 내역 정보 응답"
    )
    ResponseEntity<PaymentDto.PaymentResponse> detail(
            @Parameter(description = "결제 ID", required = true)
            @PathVariable("txNo") String txNo
    );
}
