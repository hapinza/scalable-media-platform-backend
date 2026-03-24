package io.github.catimental.diexample.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.github.catimental.diexample.Repository.RefreshTokenRepository;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.hash.TokenHashUtil;
import io.github.catimental.diexample.domain.Auth.*;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.exception.ErrorCode;
import java.util.Base64;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;
import io.github.catimental.diexample.security.JwtProvider;


@Service
@Transactional
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long REFRESH_DAYS = 14;

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProvider jwtProvider){
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
    }


   


    private String generateOpaqueToken(){
        byte[] bytes = new byte[48];

        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // create the refresh token
    @Transactional 
    public String issueAndStoreRefresh(Long memberId){
        String raw = generateOpaqueToken();
        String hash = TokenHashUtil.sha256Hex(raw);

        LocalDateTime exp = LocalDateTime.now().plusDays(REFRESH_DAYS);
        
        Member memberRef = entityManager.getReference(Member.class, memberId);
        
        // exist or not
        RefreshToken rt = refreshTokenRepository.findByMemberId(memberId)
                            .orElseGet(() -> {
                                RefreshToken x = new RefreshToken(memberRef, hash, exp);
                                return refreshTokenRepository.save(x);
                            });
                    
        //modify
        rt.setTokenHash(hash);
        rt.setExpiresAt(exp);
        rt.clearRevoked();
       
        return raw;
    }

    // update the token
    @Transactional
    public TokenPairResponse refresh(String incomingRefresh){

        String incomingHash = TokenHashUtil.sha256Hex(incomingRefresh);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(incomingHash)
                            .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "no valid token found"));

        //newHash
        Member member = rt.getMember();
        Long memberId = member.getId();
        String role = member.getRoleName();

        String newRefresh = generateOpaqueToken();
        String newHash = TokenHashUtil.sha256Hex(newRefresh);
        LocalDateTime newExp = LocalDateTime.now().plusDays(REFRESH_DAYS);


        int updated = refreshTokenRepository.rotateIfMatch(memberId, incomingHash, newHash, newExp);

        if(updated != 1){
            throw new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh token already rotated (possible concurrent refresh)");
        }

        String newAccess = jwtProvider.createAccessToken(memberId, role);

        return new TokenPairResponse(newAccess, newRefresh);
    }


    @Transactional
    public void logout(Long memberId){
        refreshTokenRepository.revokeByMemberId(memberId, LocalDateTime.now());
    }










    // public void revoke(String rawRefreshToken){
    //     String hash = TokenHashUtil.sha256Hex(rawRefreshToken);
    //     RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
    //                         .orElseThrow(() -> new ApiException(ErrorCode.REFRESH_TOKEN_INVALID, "refresh token is not valid"));
    

    //     rt.revoke();

    //     }


   






 // public String issue(Member member){
    //     String raw = generateOpaqueToken();
    //     String hash = TokenHashUtil.sha256Hex(raw);
    //     LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_DAYS);

    //     refreshTokenRepository.save(new RefreshToken(member, hash, expiresAt));
    //     return raw;
    // }


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

   

    // public void rotate(String rawRefreshToken){
    //     revoke(rawRefreshToken);
    // }


    


}
