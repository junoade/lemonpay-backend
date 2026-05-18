package com.lemonpay.payment.interfaces.api;

import com.lemonpay.payment.application.PaymentCommand;
import com.lemonpay.payment.application.PaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentUseCase paymentUseCase;

    @Override
    public ResponseEntity<PaymentDto.PaymentResponse> create(PaymentDto.CreationRequest request) {
        var result = paymentUseCase.createPendingPayment(request.toCommand());
        return ResponseEntity.ok(PaymentDto.PaymentResponse.from(result));
    }

    @Override
    public ResponseEntity<PaymentDto.PaymentResponse> approve(String txNo) {
        var command = new PaymentCommand.Approve(txNo);
        var result = paymentUseCase.approvePayment(command);
        return ResponseEntity.ok(PaymentDto.PaymentResponse.from(result));
    }

    @Override
    public ResponseEntity<PaymentDto.PaymentResponse> cancel(String txNo) {
        return null;
    }

    @Override
    public ResponseEntity<PaymentDto.PaymentResponse> detail(String txNo) {
        var command = new PaymentCommand.Query(txNo);
        var result = paymentUseCase.getDetail(command);
        return ResponseEntity.ok(PaymentDto.PaymentResponse.from(result));
    }
}
