package io.github.catimental.diexample.config;

import org.springframework.context.annotation.Bean;

public class SecurityBeansConfig {
    //return the inteface while it actually returns the class (polymorphism)
    // reason why using different name(polymorphism) is to DIP , maintenance
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}


