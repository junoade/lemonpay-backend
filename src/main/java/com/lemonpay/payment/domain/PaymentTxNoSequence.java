package com.lemonpay.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_tx_no_sequence")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTxNoSequence {

    @Id
    @Column(name = "sequence_date", nullable = false, length = 8)
    private String sequenceDate;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

    private PaymentTxNoSequence(String sequenceDate) {
        this.sequenceDate = sequenceDate;
        this.nextValue = 1L;
    }

    public static PaymentTxNoSequence create(String sequenceDate) {
        return new PaymentTxNoSequence(sequenceDate);
    }

    public Long issue() {
        Long issued = this.nextValue;
        this.nextValue++;
        return issued;
    }
}
