package com.ssafy.home.member.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.member.service.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 보호 endpoint의 Controller 실행 전에 access cookie를 검증한다.
 * 성공 시 member id를 request attribute로 넘기고 실패 시 공통 JSON 401 응답을 직접 작성해 요청을 중단한다.
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationInterceptor(
            JwtTokenService jwtTokenService,
            AuthCookieService authCookieService,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenService = jwtTokenService;
        this.authCookieService = authCookieService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            JwtClaims claims = jwtTokenService.verify(authCookieService.accessToken(request), JwtTokenType.ACCESS);
            request.setAttribute(AuthenticatedMember.REQUEST_ATTRIBUTE, claims.memberId());
            return true;
        } catch (MemberException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(exception.getMessage(), null));
            return false;
        }
    }
}
