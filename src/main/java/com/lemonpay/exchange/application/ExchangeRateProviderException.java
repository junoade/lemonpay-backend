package com.lemonpay.exchange.application;

public class ExchangeRateProviderException extends RuntimeException {

    public ExchangeRateProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExchangeRateProviderException(String message) {
        super(message);
    }
}
