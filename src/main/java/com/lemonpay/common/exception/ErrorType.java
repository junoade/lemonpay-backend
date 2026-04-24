package com.lemonpay.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    // 4xx
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_CURRENCY(HttpStatus.BAD_REQUEST, "지원하지 않는 통화입니다."),
    INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "충전 금액이 정책 범위를 벗어났습니다."),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다."),

    // 422
    WALLET_NOT_CHARGEABLE(HttpStatus.UNPROCESSABLE_ENTITY, "충전이 불가능한 지갑 상태입니다."),
    INVALID_WALLET_STATE_TRANSITION(HttpStatus.UNPROCESSABLE_ENTITY, "허용되지 않는 상태 전이입니다."),

    // 5xx
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

}
