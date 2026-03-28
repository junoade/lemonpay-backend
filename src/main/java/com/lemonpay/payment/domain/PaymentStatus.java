package com.lemonpay.payment.domain;

public enum PaymentStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    FAILED,     // 실패
    CANCELLED   // 취소
}
