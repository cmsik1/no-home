package com.ssafy.home.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 인증 정보와 사용자별 데이터를 다루는 API 응답이 브라우저나 중간 캐시에 저장되지 않게 한다.
 * Controller 처리 전 헤더를 설정하므로 정상 응답과 오류 응답에 같은 정책이 적용된다.
 */
@Component
public class ApiNoStoreFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());
        return !(path.equals("/api") || path.startsWith("/api/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        filterChain.doFilter(request, response);
    }
}
