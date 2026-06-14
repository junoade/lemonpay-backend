package com.lemonpay.common.auth;

import java.util.UUID;

/**
 * 요청 스레드에 바인딩된 인증 사용자 컨텍스트.
 *
 * <p>현재는 X-USER-ID 헤더 기반의 단순 구조이며,
 * 추후 JWT 클레임(roles, tenantId 등) 확장을 고려해 record로 정의.
 */
public record UserContext(UUID userId) {
}
