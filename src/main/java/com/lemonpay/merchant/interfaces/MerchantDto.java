package com.lemonpay.merchant.interfaces;

import com.lemonpay.merchant.domain.Merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "가맹점 API 요청 및 응답 DTO")
public class MerchantDto {

    @Schema(description = "가맹점 생성 요청")
    public record CreationRequest(
            @NotBlank String name,
            @NotBlank String callbackUrl
    ) {
        public Merchant toEntity() {
            return Merchant.create(name(), callbackUrl());
        }
    }

    @Schema(description = "가맹점 응답")
    public record MerchantResponse(
            UUID merchantId,
            String name,
            String status,
            String callbackUrl,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static MerchantResponse from(Merchant merchant) {
            return new MerchantResponse(
                    merchant.getId(),
                    merchant.getName(),
                    merchant.getStatus().name(),
                    merchant.getCallbackUrl(),
                    merchant.getCreatedAt(),
                    merchant.getUpdatedAt()
            );
        }
    }

    @Schema(description = "가맹점 목록 응답")
    public record MerchantListResponse(
            List<MerchantResponse> merchants
    ) {
        public static MerchantListResponse from(List<Merchant> merchants) {
            return new MerchantListResponse(
                    merchants.stream()
                            .map(MerchantResponse::from)
                            .toList()
            );
        }
    }
}
