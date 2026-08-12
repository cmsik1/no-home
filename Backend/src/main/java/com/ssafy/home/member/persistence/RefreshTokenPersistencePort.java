package com.ssafy.home.member.persistence;

import java.time.LocalDateTime;

/** refresh token hash의 저장·원자적 회전·폐기를 위한 영속성 계약이다. */
public interface RefreshTokenPersistencePort {
    int upsert(Long memberId, String tokenHash, LocalDateTime expiresAt);
    int rotate(Long memberId, String currentTokenHash, String newTokenHash, LocalDateTime expiresAt);
    int deleteByTokenHash(String tokenHash);
    int deleteByMemberId(Long memberId);
}
