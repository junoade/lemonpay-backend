package com.lemonpay.payment.application;

import com.lemonpay.payment.domain.PaymentTxNoSequence;
import com.lemonpay.payment.infrastructure.PaymentTxNoSequenceJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentTxNoGeneratorTest {

    @Autowired
    private PaymentTxNoGenerator paymentTxNoGenerator;

    @Autowired
    private PaymentTxNoSequenceJpaRepository repository;

    @Test
    @DisplayName("결제 거래번호는 yyyyMMdd와 10자리 순번으로 생성된다.")
    void generate_thenReturnDateAndTenDigitSequence() {
        // given
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        repository.deleteById(today);

        // when
        String firstTxNo = paymentTxNoGenerator.generate();
        String secondTxNo = paymentTxNoGenerator.generate();

        // then
        assertThat(firstTxNo).isEqualTo(today + "0000000001");
        assertThat(secondTxNo).isEqualTo(today + "0000000002");
        assertThat(firstTxNo).hasSize(18);
        assertThat(secondTxNo).hasSize(18);

        PaymentTxNoSequence sequence = repository.findById(today).orElseThrow();
        assertThat(sequence.getNextValue()).isEqualTo(3L);
    }
}
