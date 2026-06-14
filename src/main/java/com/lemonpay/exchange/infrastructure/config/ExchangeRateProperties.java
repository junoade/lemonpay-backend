package com.lemonpay.exchange.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange.rate")
public record ExchangeRateProperties(
        String provider,
        long syncTtlMinutes
) {
}
