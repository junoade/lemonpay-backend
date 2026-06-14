package com.lemonpay.exchange.interfaces;

import com.lemonpay.exchange.application.ExchangeRateSnapshot;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "환율 조회 API 요청 및 응답 DTO")
public class ExchangeRateDto {

    @Schema(description = "환율 조회 API 요청 DTO")
    public record RateRequest(
            @NotBlank String baseCurrency,
            @NotBlank String targetCurrency
    ) { }

    /**
     * @see com.lemonpay.exchange.application.ExchangeRateSnapshot
     * @param baseCurrency
     * @param targetCurrency
     * @param rate
     * @param rateDate
     * @param roundNo
     * @param rateType
     * @param source
     * @param fetchedAt
     */
    @Schema(description = "환율 조회 API 응답 DTO")
    public record RateResponse(
            String baseCurrency,
            String targetCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            int roundNo,
            String rateType,
            String source,
            LocalDateTime fetchedAt
    ) {
        public static RateResponse from(ExchangeRateSnapshot snapshot) {
            return new RateResponse(
                    snapshot.baseCurrency().getCode(),
                    snapshot.targetCurrency().getCode(),
                    snapshot.rate(),
                    snapshot.rateDate(),
                    snapshot.roundNo(),
                    snapshot.rateType().name(),
                    snapshot.source().name(),
                    snapshot.fetchedAt()
            );
        }

        public static List<RateResponse> from(List<ExchangeRateSnapshot> snapshots) {
            return snapshots.stream()
                    .map(RateResponse::from)
                    .toList();
        }
    }
}
