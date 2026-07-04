package com.lemonpay.common.config;

import com.lemonpay.common.domain.Currency;
import com.lemonpay.exchange.domain.ExchangeRate;
import com.lemonpay.exchange.domain.ExchangeRateRepository;
import com.lemonpay.exchange.domain.ExchangeRateSource;
import com.lemonpay.exchange.domain.ExchangeRateType;
import com.support.MySqlTestContainerSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TimezoneConfigTest extends MySqlTestContainerSupport {

    @Autowired ExchangeRateRepository exchangeRateRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    /**
     * 핵심 검증: JPA로 저장한 시각이 'DB 서버의 시계'와 어긋나지 않는가.
     * NOW()를 기준점으로 삼아야 쓰기/읽기 대칭 오류(±9h가 상쇄되는 경우)를 잡을 수 있음.
     */
    @Test
    @DisplayName("저장시각이 DB서버시각과 어긋나지 않는다")
    void getTimezone_thenSuccess() {
        // given: 임의 엔티티 하나 저장 (예: wallet_transaction)
        ExchangeRate savedEntity = exchangeRateRepository.save(
                ExchangeRate.create(
                        Currency.USD,
                        Currency.KRW,
                        new BigDecimal("1350.00"),
                        LocalDate.now(),
                        1,
                        ExchangeRateType.OFFICIAL,
                        ExchangeRateSource.API,
                        LocalDateTime.now()
                )
        );

        // when: 방금 저장된 행의 시각을 DB 서버 시계와 '한 쿼리 안에서' 비교한다.
        Integer diffSeconds = jdbcTemplate.queryForObject(
                "SELECT TIMESTAMPDIFF(SECOND, created_at, NOW()) " +
                        "FROM exchange_rate WHERE id = ?",
                Integer.class, savedEntity.getId());

        log.debug("JVM Timezone : {}", java.util.TimeZone.getDefault().getID());
        log.debug("user.timezone propertie : {}", System.getProperty("user.timezone"));

        // then: 9시간(32400초) 근처면 JDBC/Hibernate 세션 타임존이 어긋난 것
        assertThat(Math.abs(diffSeconds)).isLessThan(5);
    }

    @Test
    @DisplayName("MySQL 서버 자체의 타임존은 KST이다.")
    void getDbmsTimezone_thenSuccess() {
        // given & when
        String systemTz = jdbcTemplate.queryForObject("SELECT @@system_time_zone", String.class);
        // then
        assertThat(systemTz).isEqualTo("KST");
    }

    /**
     * @@session.time_zone은 'SYSTEM'을 반환할 수 있어(= system_time_zone에 위임) 문자열 단언은 취약함.
     * 따라서 설정값이 아니라 '실제 해석된 UTC 오프셋'을 검증.
     * 현재 Timezone ADR(전 레이어 KST 고정)에 따라 +09:00 이어야 함.
     */
    @Test
    @DisplayName("JDBC 세션 타임존은 실질적으로 KST 오프셋이다.")
    void getJdbcSessionTimezone_thenSuccess() {
        // given & when
        String offset = jdbcTemplate.queryForObject(
                "SELECT TIMEDIFF(NOW(), UTC_TIMESTAMP())", String.class);
        // then
        assertThat(offset).isEqualTo("09:00:00");
    }

}
