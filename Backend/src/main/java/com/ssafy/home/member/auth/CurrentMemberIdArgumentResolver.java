package com.ssafy.home.member.auth;

import com.ssafy.home.member.service.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentMemberId}가 붙은 Controller parameter를 인증된 회원 id로 변환한다.
 * Interceptor가 넣은 request attribute를 우선 사용하고, 선택 인증 endpoint에서는 cookie 검증을 fallback으로 사용한다.
 */
@Component
public class CurrentMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenService jwtTokenService;
    private final AuthCookieService authCookieService;

    public CurrentMemberIdArgumentResolver(JwtTokenService jwtTokenService, AuthCookieService authCookieService) {
        this.jwtTokenService = jwtTokenService;
        this.authCookieService = authCookieService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMemberId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        Object authenticated = request.getAttribute(AuthenticatedMember.REQUEST_ATTRIBUTE);
        if (authenticated instanceof Long memberId) {
            return memberId;
        }
        try {
            JwtClaims claims = jwtTokenService.verify(authCookieService.accessToken(request), JwtTokenType.ACCESS);
            return claims.memberId();
        } catch (MemberException exception) {
            return null;
        }
    }
}
