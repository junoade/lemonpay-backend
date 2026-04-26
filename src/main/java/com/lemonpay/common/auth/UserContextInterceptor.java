package com.lemonpay.common.auth;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * X-USER-ID 헤더를 파싱해 UserContextHolder에 바인딩하는 인터셉터.
 *
 * <p>추후 JWT 기반 인증으로 전환 시, preHandle()의 파싱 로직만 교체하면
 * Controller/UseCase 코드 변경 없이 고도화 가능하다.
 *
 * <pre>
 * 현재: X-USER-ID 헤더 → userId 추출
 * 추후: Authorization: Bearer <JWT> → 파싱 → userId, roles 등 추출
 * </pre>
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-USER-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdHeader = request.getHeader(HEADER_USER_ID);

        if (userIdHeader == null || userIdHeader.isBlank()) {
            log.warn("인증 헤더 누락: path={}", request.getRequestURI());
            throw new CoreException(ErrorType.UNAUTHORIZED, "X-USER-ID 헤더가 필요합니다.");
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);
            UserContextHolder.set(new UserContext(userId));
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 X-USER-ID 헤더값: {}", userIdHeader);
            throw new CoreException(ErrorType.INVALID_REQUEST, "X-USER-ID가 유효하지 않습니다.");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // ThreadLocal 메모리 누수 방지 (thread pool 재사용 환경)
        UserContextHolder.clear();
    }
}
