package com.lemonpay.common.auth;

import com.lemonpay.common.exception.CoreException;
import com.lemonpay.common.exception.ErrorType;

import java.util.UUID;

/**
 * 요청 스레드에 UserContext를 바인딩하는 홀더.
 *
 * <p>ThreadLocal을 사용하므로 요청이 끝나면 반드시 clear()를 호출해야
 * 메모리 누수(thread pool 재사용 시 이전 컨텍스트 잔류)를 방지할 수 있다.
 * 인터셉터의 afterCompletion()에서 처리한다.
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserContext context) {
        HOLDER.set(context);
    }

    public static UserContext get() {
        return HOLDER.get();
    }

    public static UUID getUserId() {
        UserContext context = HOLDER.get();
        if (context == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "UserContext가 설정되지 않았습니다. X-USER-ID 헤더를 확인하세요.");
        }
        return context.userId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
