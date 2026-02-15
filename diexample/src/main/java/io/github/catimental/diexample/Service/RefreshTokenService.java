package io.github.catimental.diexample.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;
import io.github.catimental.diexample.Repository.RefreshTokenRepository;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.hash.TokenHashUtil;
import io.github.catimental.diexample.domain.Auth.*;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.exception.ErrorCode;

@Service
@Transactional
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long REFRESH_DAYS = 14;


    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public String issue(Member member){
        String raw = generateOpaqueToken();
        String hash = TokenHashUtil.sha256Hex(raw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_DAYS);

        refreshTokenRepository.save(new RefreshToken(member, hash, expiresAt));
        return raw;
    }


    private String generateOpaqueToken(){
        byte[] bytes = new byte[48];

        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    public Member validateAndGetMember(String rawRefreshToken){

        if(rawRefreshToken == null || rawRefreshToken.isBlank()){
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh token is required");
        }

        String hash = TokenHashUtil.sha256Hex(rawRefreshToken);
        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                            .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh token is not valid"));

        if(rt.isRevoked()){
            throw new ApiException(ErrorCode.REFRESH_TOKEN_REVOKED, "refresh token has been revoked");
        }

        if(rt.isExpired()){
            throw new ApiException(ErrorCode.REFRESH_TOKEN_EXPIRED, "refresh token has been expired");
        }

        return rt.getMember();
    }

    public void revoke(String rawRefreshToken){
        String hash = TokenHashUtil.sha256Hex(rawRefreshToken);
        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                            .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh token is not valid"));
    

        rt.revoke();

        }


    public void rotate(String rawRefreshToken){
        revoke(rawRefreshToken);
    }


    


}
