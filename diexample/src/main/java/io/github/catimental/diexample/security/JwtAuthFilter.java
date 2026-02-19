package io.github.catimental.diexample.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;


/*
Http request -> JWT encoding(filter) -> verification ->
convert it to spring object -> store in SecurityContext box
-> operate in controller and service
*/


public class JwtAuthFilter extends OncePerRequestFilter{

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider){
        this.jwtProvider = jwtProvider;
    }
    

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{

    String header = request.getHeader("Authorization");

    if(header != null && header.startsWith("Bearer ")){
        String token = header.substring(7);

        try{
            Claims claims = jwtProvider.parseClaims(token);
            Long memberId = Long.valueOf(claims.getSubject());
            String role = String.valueOf(claims.get("role"));


            var auth = new UsernamePasswordAuthenticationToken(
                memberId, 
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch(Exception e){
            SecurityContextHolder.clearContext();
        }
    }

    filterChain.doFilter(request, response); // next step with this data
    }

    





}
