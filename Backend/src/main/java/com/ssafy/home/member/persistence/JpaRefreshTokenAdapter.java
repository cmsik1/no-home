package com.ssafy.home.member.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class JpaRefreshTokenAdapter implements RefreshTokenPersistencePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public int upsert(Long memberId, String tokenHash, LocalDateTime expiresAt) {
        return entityManager.createNativeQuery("""
                        INSERT INTO member_refresh_tokens (member_id, token_hash, expires_at)
                        VALUES (:memberId, :tokenHash, :expiresAt)
                        ON CONFLICT (member_id) DO UPDATE SET
                            token_hash = EXCLUDED.token_hash,
                            expires_at = EXCLUDED.expires_at,
                            updated_at = CURRENT_TIMESTAMP
                        """)
                .setParameter("memberId", memberId)
                .setParameter("tokenHash", tokenHash)
                .setParameter("expiresAt", expiresAt)
                .executeUpdate();
    }

    @Override
    public int rotate(Long memberId, String currentTokenHash, String newTokenHash, LocalDateTime expiresAt) {
        return entityManager.createNativeQuery("""
                        UPDATE member_refresh_tokens
                        SET token_hash = :newTokenHash,
                            expires_at = :expiresAt,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE member_id = :memberId
                          AND token_hash = :currentTokenHash
                        """)
                .setParameter("memberId", memberId)
                .setParameter("currentTokenHash", currentTokenHash)
                .setParameter("newTokenHash", newTokenHash)
                .setParameter("expiresAt", expiresAt)
                .executeUpdate();
    }

    @Override
    public int deleteByTokenHash(String tokenHash) {
        return entityManager.createNativeQuery("DELETE FROM member_refresh_tokens WHERE token_hash = :tokenHash")
                .setParameter("tokenHash", tokenHash)
                .executeUpdate();
    }

    @Override
    public int deleteByMemberId(Long memberId) {
        return entityManager.createNativeQuery("DELETE FROM member_refresh_tokens WHERE member_id = :memberId")
                .setParameter("memberId", memberId)
                .executeUpdate();
    }
}
