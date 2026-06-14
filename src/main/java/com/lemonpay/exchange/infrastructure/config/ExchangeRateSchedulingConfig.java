package com.lemonpay.exchange.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "exchange.rate.scheduler.enabled",
        havingValue = "true"
)
public class ExchangeRateSchedulingConfig {
}
