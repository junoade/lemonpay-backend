package com.lemonpay.config;

import com.lemonpay.common.auth.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 인증 불필요 API (로그인, 회원가입)
                        "/api/v1/members/login",
                        "/api/v1/members/join",
                        "/api/v1/merchants/**",
                        // Swagger UI (dev/local 환경에서만 활성화되므로 인터셉터 제외)
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // 헬스체크 (모니터링 시스템에서 인증 없이 호출)
                        "/actuator/health"
                );
    }
}
