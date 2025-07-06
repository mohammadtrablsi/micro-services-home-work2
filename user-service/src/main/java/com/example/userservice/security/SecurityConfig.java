package com.example.userservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;




@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Autowired
    private HeaderAuthFilter headerAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // public endpoints
            .requestMatchers("/auth/login",
                             "/auth/register/learner",
                             "/auth/register/admin").permitAll()

            // trainer registration still restricted to admins
            .requestMatchers("/auth/register/trainer").hasRole("ADMIN")
            // .requestMatchers("/users/*/balance", "/users/*/deduct").permitAll()
            .requestMatchers("/auth/users/*/deduct", "/auth/users/*/balance").permitAll()

            // ✅ FIXED: use the full path prefix
            .requestMatchers("/auth/users/name/**").hasRole("ADMIN")
            //        or .permitAll() if you don’t need a token

            .anyRequest().authenticated()
        )
        .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
