package com.ppcex.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/market/api/v1/public/**"),
                                new AntPathRequestMatcher("/market/doc.html"),
                                new AntPathRequestMatcher("/market/swagger-ui/**"),
                                new AntPathRequestMatcher("/market/v3/api-docs/**"),
                                new AntPathRequestMatcher("/market/actuator/**"),
                                new AntPathRequestMatcher("/market/favicon.ico"),
                                new AntPathRequestMatcher("/market/error")
                        ).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}