package com.lemonpay.merchant.interfaces;

import com.lemonpay.merchant.domain.Merchant;
import com.lemonpay.merchant.domain.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantV1Controller implements MerchantV1ApiSpec {

    private final MerchantService merchantService;

    @Override
    public ResponseEntity<MerchantDto.MerchantListResponse> getMerchants() {
        List<Merchant> merchants = merchantService.getMerchants();
        return ResponseEntity.ok(MerchantDto.MerchantListResponse.from(merchants));
    }

    @Override
    public ResponseEntity<MerchantDto.MerchantResponse> create(MerchantDto.CreationRequest request) {
        Merchant merchant = merchantService.register(request.toEntity());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MerchantDto.MerchantResponse.from(merchant));
    }

    @Override
    public ResponseEntity<MerchantDto.MerchantResponse> getMerchant(UUID merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        return ResponseEntity.ok(MerchantDto.MerchantResponse.from(merchant));
    }
}
