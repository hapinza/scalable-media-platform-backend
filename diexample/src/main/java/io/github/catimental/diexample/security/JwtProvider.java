package io.github.catimental.diexample.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;



@Component
public class JwtProvider {
    
    private final Key key;  // signature , key+ HMAC-SHA256d -> check the string in JWT
    private final long accessTokenValidityMs; // expiration date

   // @Value("${jwt.secret}")
   //   @Value("${jwt.access-token-validity-ms}")
    public JwtProvider( String secret, long accessTokenValidityMs){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityMs = accessTokenValidityMs;
    }

    // create token for user
    // JWT lifecycle
    public String createAccessToken(Long memberId, String role){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);


        return Jwts.builder()
                .setSubject(String.valueOf(memberId)) // sub = memberId
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
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
