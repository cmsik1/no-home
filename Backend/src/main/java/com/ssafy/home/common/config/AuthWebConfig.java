package com.ssafy.home.common.config;

import com.ssafy.home.member.auth.JwtAuthenticationInterceptor;
import com.ssafy.home.member.auth.CurrentMemberIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

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
