package com.lemonpay.exchange.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "exchange.rate.scheduler")
public record ExchangeRateSchedulerProperties(
        boolean enabled,
        String cron,
        List<String> pairs
) {
}
