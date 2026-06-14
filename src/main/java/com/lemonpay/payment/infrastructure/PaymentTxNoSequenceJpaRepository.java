package com.lemonpay.payment.infrastructure;

import com.lemonpay.payment.domain.PaymentTxNoSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentTxNoSequenceJpaRepository extends JpaRepository<PaymentTxNoSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PaymentTxNoSequence s where s.sequenceDate = :sequenceDate")
    Optional<PaymentTxNoSequence> findBySequenceDateForUpdate(@Param("sequenceDate") String sequenceDate);
}
