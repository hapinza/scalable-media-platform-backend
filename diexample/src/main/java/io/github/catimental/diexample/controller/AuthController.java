package io.github.catimental.diexample.controller;


import java.lang.annotation.Repeatable;

import org.apache.catalina.connector.Response;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.refreshToken.TokenPairResponse;
import io.github.catimental.diexample.DTO.refreshToken.TokenRefreshRequest;
import io.github.catimental.diexample.Service.RefreshTokenService;
import io.github.catimental.diexample.Service.audit.AuditLogService;
import io.github.catimental.diexample.common.util.ClientIpUtil;
import io.github.catimental.diexample.security.*;
import jakarta.servlet.http.HttpServlet;
import io.github.catimental.diexample.domain.Member;
import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.DTO.refreshToken.LogoutRequest;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final AuditLogService auditLogService;

    public AuthController(RefreshTokenService refreshTokenService, JwtProvider jwtProvider, AuditLogService auditLogService){
        this.refreshTokenService = refreshTokenService;
        this.jwtProvider = jwtProvider;
        this.auditLogService = auditLogService;
    }


    private String ua(HttpServletRequest req){
        String v = req.getHeader("User-Agent");
        return v == null ? v : v.substring(0, Math.min(200, v.length()));

    }



    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponse> refresh(@RequestBody TokenRefreshRequest req, Authentication authentication,
        HttpServletRequest http
    ){

        String ip = ClientIpUtil.getClientIp(http);
        String userAgent = ua(http);

        try{
            Member member = refreshTokenService.validateAndGetMember(req.refreshToken());

            refreshTokenService.rotate(req.refreshToken());

            String newFresh = refreshTokenService.issue(member);

            String newAccess = jwtProvider.createAccessToken(member.getId(), member.assignUserRole());

            auditLogService.log(member.getId(), "REFRESH", true, null, ip, userAgent);

            return ResponseEntity.ok(new TokenPairResponse(newAccess, newFresh));

        }catch(ApiException e){
            auditLogService.log(member.getId(), "REFRESH_FAIL", false, e.getErrorCode().name(), ip, userAgent);
            throw e;
        }
    
        
        return ResponseEntity.ok(new TokenPairResponse(newAccess, newFresh));
    }

    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Requestbody LogoutRequest req,
        HttpServletRequest http
    ){
       
        String ip = ClientIpUtil.getClientIp(http);
        String userAgent = ua(http);
        try{
            // token revoke 
            
            Member member = refreshTokenService.validateAndGetMember(req.refreshToken());
            refreshTokenService.revoke(req.refreshToken());

            auditLogService.log(member.getId(), "LOUTOUT", true, null, ip, userAgent);

        }catch(ApiException e){
            auditLogService.log(null, "LOGOUT_FAIL", false, e.getErrorCode().name(), ip, userAgent);
            throw e;
        }



        return ResponseEntity.ok().build();
    }
}
