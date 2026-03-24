package io.github.catimental.diexample.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.github.catimental.diexample.Service.RateLimit.RateLimitFilter;
import io.github.catimental.diexample.Service.RateLimit.RedisRateLimiter;


@Configuration
public class SecurityConfig {
    
    @Bean
    public JwtProvider jwtProvider(){
        String secret = "change-this-secret-to-at-least-32-bytes";
        return new JwtProvider(secret, 1000L*60*15);
    }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider, RedisRateLimiter limiter) throws Exception{
            http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())

                .contentTypeOptions(cto -> {})

                .referrerPolicy(rp -> rp.policy(
                    org.springframework.security.web.header.writers
                    .ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                ))

                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/members/register", "/api/members/login", "/auth/refresh", "/auth/logout",
                "/movies/trending", "/analytics/views/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new RateLimitFilter(limiter), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }




}
