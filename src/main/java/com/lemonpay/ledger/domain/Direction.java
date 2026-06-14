package com.lemonpay.ledger.domain;

public enum Direction {
    /**
     * 차변
     * Wallet 기준: 잔액 감소 (outflow)
     * <b>회사 회계 기준: 고객 자산 감소 → 부채 감소 (차변)</b>
     */
    DEBIT,

    /**
     * 대변
     * Wallet 기준: 잔액 증가 (inflow)
     * <b>회사 회계 기준: 고객 자산 증가 -> 부채 증가 (대변)</b>
     */
    CREDIT,
}
