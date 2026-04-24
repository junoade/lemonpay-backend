package com.lemonpay.common.interfaces;

import com.lemonpay.common.exception.ErrorType;

public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse of(ErrorType errorType) {
        return new ErrorResponse(errorType.name(), errorType.getMessage());
    }

    public static ErrorResponse of(ErrorType errorType, String detail) {
        return new ErrorResponse(errorType.name(), detail);
    }
}
