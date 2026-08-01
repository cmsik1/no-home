package com.ssafy.home.member.persistence;

import java.time.LocalDateTime;

public interface RefreshTokenPersistencePort {
    int upsert(Long memberId, String tokenHash, LocalDateTime expiresAt);
    int rotate(Long memberId, String currentTokenHash, String newTokenHash, LocalDateTime expiresAt);
    int deleteByTokenHash(String tokenHash);
    int deleteByMemberId(Long memberId);
}
