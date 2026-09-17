package com.lemonpay.common.interfaces;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * API 응답 시각 변환 유틸.
 *
 * ADR-002: 내부 시각은 KST 벽시계(LocalDateTime)로 다루되,
 * API 경계에서는 클라이언트가 타임존을 추측하지 않도록 오프셋(+09:00)을 명시해 직렬화한다.
 * (Zone 없는 직렬화는 클라이언트별 해석 차이로 표시 시각이 밀리는 버그를 유발함)
 */
public final class ApiTime {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private ApiTime() { }

    /**
     * KST 벽시계 시각에 서비스 타임존 오프셋(+09:00)을 명시함
     * 미완료 시각 필드(completedAt 등)를 위해 null 입력은 null을 반환
     */
    public static OffsetDateTime toOffset(LocalDateTime wallClock) {
        return wallClock == null ? null : wallClock.atZone(SERVICE_ZONE).toOffsetDateTime();
    }
}
