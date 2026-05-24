package com.lemonpay.merchant.domain;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    @Transactional(readOnly = true)
    public Merchant getMerchantById(UUID id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public void validateMerchantPayable(UUID id) {
        Merchant found = getMerchantById(id);
        found.validatePayable();
    }

    @Transactional
    public Merchant register(Merchant merchant) {
        if(merchantRepository.existsById(merchant.getId())) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "이미 등록된 가맹점입니다.");
        }
        return merchantRepository.save(merchant);
    }

    @Transactional
    public Merchant update(Merchant merchant) {
        if(!merchantRepository.existsById(merchant.getId())) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }

        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public List<Merchant> getMerchants() {
        return merchantRepository.findAll();
    }
  
    @Transactional(readOnly = true)
    public Merchant getPayableMerchant(UUID merchantId) {
        Merchant merchant = getMerchantById(merchantId);
        merchant.validatePayable();
        return merchant;
    }

}
