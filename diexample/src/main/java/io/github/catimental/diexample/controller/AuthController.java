package io.github.catimental.diexample.controller;


import java.lang.annotation.Repeatable;

import org.apache.catalina.connector.Response;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;
import io.github.catimental.diexample.DTO.refreshToken.TokenRefreshRequest;
import io.github.catimental.diexample.Service.RefreshTokenService;
import io.github.catimental.diexample.security.*;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.DTO.refreshToken.LogoutRequest;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    public AuthController(RefreshTokenService refreshTokenService){
        this.refreshTokenService = refreshTokenService;
        this.jwtProvider = jwtProvider;
    }



    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody TokenRefreshRequest req, Authentication authentication){
        
        Member member = refreshTokenService.validateAndGetMember(req.refreshToken());

        refreshTokenService.rotate(req.refreshToken());
        String newFresh = refreshTokenService.issue(member);

        String newAccess = jwtProvider.createAccessToken(member.getId(), member.assignUserRole());
        
        return ResponseEntity.ok(new TokenPairResponse(newAccess, newFresh));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Requestbody LogoutRequest req){
        refreshTokenService.revoke(req.refreshToken());
        return ResponseEntity.ok().build();
    }
}
