package com.ssafy.home.member.auth;

import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.persistence.RefreshTokenPersistencePort;
import com.ssafy.home.member.service.MemberErrorCode;
import com.ssafy.home.member.service.MemberException;
import com.ssafy.home.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 로그인, refresh token 회전과 로그아웃의 트랜잭션 경계다.
 * access/refresh JWT를 발급하고 refresh token 원문 대신 SHA-256 hash와 만료 시각만 DB에 저장한다.
 */
@Service
public class MemberAuthService {

    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenPersistencePort refreshTokenPersistencePort;

    public MemberAuthService(
            MemberService memberService,
            JwtTokenService jwtTokenService,
            RefreshTokenPersistencePort refreshTokenPersistencePort
    ) {
        this.memberService = memberService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenPersistencePort = refreshTokenPersistencePort;
    }

    /**
     * 자격 증명 확인과 token 발급·저장을 한 유스케이스로 묶는다.
     * 반환된 원문 token은 Controller가 cookie로 전달하고 DB에는 refresh token hash만 남는다.
     */
    @Transactional
    public LoginResult login(String email, String password) {
        MemberResponse member = memberService.login(email, password);
        JwtTokenPair tokens = jwtTokenService.issue(member.memberId());
        refreshTokenPersistencePort.upsert(member.memberId(), TokenHash.sha256(tokens.refreshToken()),
                LocalDateTime.ofInstant(tokens.refreshExpiresAt(), ZoneOffset.UTC));
        return new LoginResult(member, tokens);
    }

    @Transactional
    public JwtTokenPair refresh(String refreshToken) {
        JwtClaims claims = jwtTokenService.verify(refreshToken, JwtTokenType.REFRESH);
        JwtTokenPair newTokens = jwtTokenService.issue(claims.memberId());
        int rotated = refreshTokenPersistencePort.rotate(
                claims.memberId(),
                TokenHash.sha256(refreshToken),
                TokenHash.sha256(newTokens.refreshToken()),
                LocalDateTime.ofInstant(newTokens.refreshExpiresAt(), ZoneOffset.UTC)
        );
        if (rotated != 1) {
            throw new MemberException(MemberErrorCode.UNAUTHENTICATED, "refresh token is no longer valid.");
        }
        return newTokens;
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenPersistencePort.deleteByTokenHash(TokenHash.sha256(refreshToken));
        }
    }

    @Transactional
    public void revokeMember(Long memberId) {
        refreshTokenPersistencePort.deleteByMemberId(memberId);
    }

    public record LoginResult(MemberResponse member, JwtTokenPair tokens) {
    }
}
