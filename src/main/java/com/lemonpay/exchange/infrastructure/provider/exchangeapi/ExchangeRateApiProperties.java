package com.lemonpay.exchange.infrastructure.provider.exchangeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "exchange.rate-api")
public record ExchangeRateApiProperties(
        String baseUrl,
        String apiKey
) {

    public boolean hasApiKey() {
        return StringUtils.hasText(apiKey);
    }
}
