package io.github.catimental.diexample.Service.RateLimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;

import javax.print.attribute.standard.Media;

import io.github.catimental.diexample.common.util.*;

public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RedisRateLimiter limiter;

    private static final int LIMIT = 60;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitFilter(RedisRateLimiter limiter){
        this.limiter = limiter;
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getRequestURI();
        return path.startsWith("/api/members/") ||
                path.startsWith("/api/members/register") ||
                path.startsWith("/auth/") ||
                path.startsWith("/actuator");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain
    ) throws ServletException, IOException{
        String clientIp = ClientIpUtil.getClientIp(request);
        String route = request.getRequestURI();


        //long minuteBucket = System.currentTimeMillis() / 60000;

        String key = "rl:ip:" + clientIp + ":route:" + route;
        
        long count = limiter.incrementAndGet(key, WINDOW_SECONDS);

        if(count > LIMIT){
            Long ttl = limiter.getTtSeconds(key);
            long retryAfter = (ttl == null || ttl < 0) ? WINDOW_SECONDS : ttl;


            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"error\":\"rate_limit_exceeded\",\"limit\":" +  LIMIT +
                ",\"windowSeconds\":" + WINDOW_SECONDS + 
                ",\"retryAfterSeconds\":" + retryAfter + "}"
            );


            return;
        }


        filterChain.doFilter(request, response);

    }
}
