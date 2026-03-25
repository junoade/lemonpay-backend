package com.lemonpay.common.domain;

/**
 * 지원 통화 코드
 *
 * <p>통화별 소수점 자릿수(scale), </p>
 */
public enum Currency {
    KRW(0, "원", "KRW", "₩"),
    USD(2, "달러", "USD", "$"),
    JPY(0, "엔", "JPY", "¥");

    private final int scale;
    private final String name;
    private final String code;
    private final String symbol;

    Currency(int scale, String name, String code, String symbol) {
        this.scale = scale;
        this.name = name;
        this.code = code;
        this.symbol = symbol;
    }

    public int getScale() {
        return scale;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }
}