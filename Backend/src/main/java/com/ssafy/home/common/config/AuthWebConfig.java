package com.ssafy.home.common.config;

import com.ssafy.home.member.auth.JwtAuthenticationInterceptor;
import com.ssafy.home.member.auth.CurrentMemberIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

/**
 * cookie 기반 JWT 인증을 Spring MVC 요청 처리 과정에 연결한다.
 * Interceptor가 검증한 회원 id는 {@code @CurrentMemberId} argument resolver를 통해 Controller로 전달된다.
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    public AuthWebConfig(
            JwtAuthenticationInterceptor jwtAuthenticationInterceptor,
            CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver
    ) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
        this.currentMemberIdArgumentResolver = currentMemberIdArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/members/me", "/api/members/search", "/api/ai/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberIdArgumentResolver);
    }
}
