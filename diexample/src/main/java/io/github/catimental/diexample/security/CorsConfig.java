package io.github.catimental.diexample.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
public class CorsConfig {
  
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
      "http//localhost:3000"
    ));

     
      //OPTIONS (browser automatically sends): inspect Origin, HTTP Method, Header
      // haeder is basically protected compared to body
      config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      config.setAllowedHeaders(List.of("Authorization", "Content-Type")); // browser -> server
      config.setExposedHeaders(List.of("Authorization")); // server -> browser 
      config.setAllowCredentials(false); // cookie  
      config.setMaxAge(3600L);


        // cookie

        // Preflight(OPSIONS)
        // Cache the CORS preflight (OPTIONS) response in the browser for 1 hour.
        // If the same origin, method, and headers are used, the browser skips
        // the preflight check and sends the actual request directly.
        // 
        /*
            no CORS + simple, request
            CORS + simple , request
            CORS + non simple , OPTIONS
        */


      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

      source.registerCorsConfiguration("/**", config);

      return source;  
    }
  }






