package io.github.catimental.diexample.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import io.github.catimental.diexample.domain.Auth.*;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByMemberId(Long memberId);

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.tokenHash = :newHash, rt.expiresAt = :newExp
            where rt.member.id = :memberId
            and rt.tokenHash = :oldHash    
            """)
    int rotateIfMatch(Long memberId, String oldHash, String newHash, LocalDateTime newExp);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update RefreshToken rt
           set rt.revokedAt = :revokedAt
         where rt.member.id = :memberId
           and rt.revokedAt is null
    """)
    int revokeByMemberId(Long memberId, LocalDateTime revokedAt);
    
}
