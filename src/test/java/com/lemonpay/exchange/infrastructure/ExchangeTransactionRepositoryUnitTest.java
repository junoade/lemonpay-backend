package com.lemonpay.exchange.infrastructure;

import com.lemonpay.exchange.domain.ExchangeTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeTransactionRepositoryUnitTest {

    @Mock
    private ExchangeTransactionJpaRepository jpaRepository;

    @InjectMocks
    private ExchangeTransactionRepositoryImpl repository;

    @Test
    @DisplayName("환전 거래 저장을 JPA 레포지토리에 위임하고 저장 결과를 반환한다.")
    void savesTransaction() {
        // given
        ExchangeTransaction transaction = mock(ExchangeTransaction.class);
        ExchangeTransaction saved = mock(ExchangeTransaction.class);
        when(jpaRepository.save(transaction)).thenReturn(saved);

        // when
        ExchangeTransaction result = repository.save(transaction);

        // then
        verify(jpaRepository).save(transaction);
        assertThat(result).isSameAs(saved);
    }
}
