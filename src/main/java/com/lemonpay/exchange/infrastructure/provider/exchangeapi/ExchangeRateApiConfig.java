package com.lemonpay.exchange.infrastructure.provider.exchangeapi;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ExchangeRateApiProperties.class)
public class ExchangeRateApiConfig {

    @Bean
    public RestClient exchangeRateRestClient(
            RestClient.Builder builder,
            ExchangeRateApiProperties properties
    ) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
