package io.github.catimental.diexample.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;



public class SecurityConfig {
    
    @Bean
    public JwtProvider jwtProvider(){
        String secret = "change-this-secret-to-at-least-32-bytes";
        return new JwtProvider(secret, 1000L*60*15);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception{
        http
        .csrf(csrf -> csrf.disable())
        .cors(Costomizer.withDefault())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/members/register", "/members/login", "/auth/refresh", "/auth/logout",
            "/movies/trending", "/analytics/views/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }




}
