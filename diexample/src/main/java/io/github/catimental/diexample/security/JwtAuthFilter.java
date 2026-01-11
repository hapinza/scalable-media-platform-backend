package io.github.catimental.diexample.security;

import io.jasonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

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
                                    FilterChain filteChain) throws ServletException, IOException{

    String haeder = request.getHeader("Authorization");

    if(haeder != null && header.startsWith("Bear ")){
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
