package com.lemonpay.ledger.domain;

public enum EntryType {
    CHARGE(Direction.CREDIT),        // 충전
    PAYMENT(Direction.DEBIT),        // 결제차감
    FX_WITHDRAW(Direction.DEBIT),    // 환전출금
    FX_DEPOSIT(Direction.CREDIT);    // 환전입금

    private final Direction direction;

    EntryType(Direction direction) { this.direction = direction; }
    public Direction getDirection() { return direction; }
}
