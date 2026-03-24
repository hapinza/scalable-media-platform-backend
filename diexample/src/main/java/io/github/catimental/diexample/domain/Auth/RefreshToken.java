package io.github.catimental.diexample.domain.Auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import io.github.catimental.diexample.domain.Member;

// import org.springframework.cglib.core.Local;


@Entity
@Table (
    name = "refresh_token", indexes ={
        @Index(name = "idx_refresh_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_refresh_member", columnList = "member_id")
    }
)
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false , unique = true)
    private Member member;


    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;


    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected RefreshToken(){}

    public RefreshToken(Member member, String tokenHash, LocalDateTime expiresAt){
        this.member = member;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked(){
        return revokedAt != null;
    }

    public void revoke(){
        this.revokedAt = LocalDateTime.now();
    }

    public void clearRevoked(){
        this.revokedAt = null;
    }

    public Member getMember(){return this.member;}
    public String getTokenHash(){return this.tokenHash;}
    public LocalDateTime getExpireAt(){return this.expiresAt;}

    public void setTokenHash(String newHash){
        this.tokenHash = newHash;
    }
    
    public void setExpiresAt(LocalDateTime exp){
        this.expiresAt = exp;
    }



}
