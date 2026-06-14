package com.lemonpay.exchange.infrastructure.config;

import com.lemonpay.exchange.application.ExchangeRateSyncPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties({ExchangeRateProperties.class, ExchangeRateSchedulerProperties.class})
public class ExchangeRateConfig {

    @Bean
    public ExchangeRateSyncPolicy exchangeRateSyncPolicy(ExchangeRateProperties exchangeRateProperties) {
        return new ExchangeRateSyncPolicy(Duration.ofMinutes(exchangeRateProperties.syncTtlMinutes()));
    }
}
