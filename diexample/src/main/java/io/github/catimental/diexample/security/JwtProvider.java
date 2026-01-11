package io.github.catimental.diexample.security;

import io.jsonwebtoekn.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.securityKeys;

import java.security.Key;
import java.util.Date;




public class JwtProvider {
    
    private final Key key;  // signature 
    private final long accesstTokenValidityMs; // expiration date

    public JwtProvider(String secret, long accessTokenValidityMs){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    // create token for user
    public String createAccessToken(Long memberId, String role){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accesstTokenValidityMs);


        return Jwts.builder()
                .setSubject(String.valueOf(memberId)) // sub = memberId
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.H256)
                .compact();
    }


    public Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();


    }



}
