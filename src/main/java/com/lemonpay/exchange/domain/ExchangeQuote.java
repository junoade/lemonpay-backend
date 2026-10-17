package com.lemonpay.exchange.domain;

import com.lemonpay.common.domain.Money;

import java.math.BigDecimal;

public record ExchangeQuote(
        Money baseMoney,
        Money targetMoney,
        BigDecimal appliedRate
) {
    /**
     * 기준 통화 금액을 기준으로 목표 통화 금액을 계산하여 환전 견적을 생성합니다.
     * <p>일반 환전처럼 사용자가 환전할 기준 통화 금액을 지정하는 경우 사용합니다.
     *
     * <p>예: 100,000 KRW를 JPY로 환전
     *
     * @param baseMoney     환전할 기준 통화 금액
     * @param exchangeRate  기준 통화에서 목표 통화로 적용할 환율
     * @return              계산된 목표 통화 금액을 포함한 환전 견적
     */
    public static ExchangeQuote fromBaseAmount(Money baseMoney, ExchangeRate exchangeRate) {
        Money targetMoney = exchangeRate.convertBaseToTarget(baseMoney);
        return new ExchangeQuote(baseMoney, targetMoney, exchangeRate.getRate());
    }

    /**
     * 목표 통화 금액을 기준으로 필요한 기준 통화 금액을 역산하여 환전 견적을 생성합니다.
     * <p>부족 금액 자동 환전처럼 확보해야 할 목표 통화 금액이 정해진 경우 사용합니다.
     *
     * <p>예시: 결제에 부족한 70 USD를 확보하기 위해 필요한 KRW 계산
     *
     * @param targetMoney   확보해야 할 목표 통화 금액
     * @param exchangeRate  기준 통화에서 목표 통화로 적용할 환율
     * @return              역산된 기준 통화 금액을 포함한 환전 견적
     */
    public static ExchangeQuote forTargetAmount(Money targetMoney, ExchangeRate exchangeRate) {
        Money baseMoney = exchangeRate.convertTargetToBase(targetMoney);
        return new ExchangeQuote(baseMoney, targetMoney, exchangeRate.getRate());
    }
}
