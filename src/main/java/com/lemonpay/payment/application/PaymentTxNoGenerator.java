package com.lemonpay.payment.application;

import com.lemonpay.payment.domain.PaymentTxNoSequence;
import com.lemonpay.payment.infrastructure.PaymentTxNoSequenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PaymentTxNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final PaymentTxNoSequenceJpaRepository repository;

    @Transactional
    public String generate() {
        String sequenceDate = LocalDate.now().format(DATE_FORMATTER);

        PaymentTxNoSequence sequence = repository.findBySequenceDateForUpdate(sequenceDate)
                .orElseGet(() -> repository.save(PaymentTxNoSequence.create(sequenceDate)));

        Long sequenceValue = sequence.issue();

        return "%s%010d".formatted(sequenceDate, sequenceValue);
    }
}
