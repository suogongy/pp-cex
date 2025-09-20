package com.ppcex.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 网关安全配置
 *
 * @author PPCEX Team
 * @version 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // API文档相关路径允许匿名访问
                .pathMatchers("/doc.html").permitAll()
                .pathMatchers("/doc.html/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/swagger-ui.html").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers("/v3/api-docs").permitAll()
                .pathMatchers("/v3/api-docs/swagger-config").permitAll()
                .pathMatchers("/swagger-resources/**").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                // Knife4j UI相关路径
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                // 认证相关路径允许匿名访问
                .pathMatchers("/api/v1/auth/login").permitAll()
                .pathMatchers("/api/v1/auth/register").permitAll()
                .pathMatchers("/api/v1/auth/refresh").permitAll()
                .pathMatchers("/api/v1/public/**").permitAll()
                // 健康检查和监控路径允许匿名访问
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/health").permitAll()
                .pathMatchers("/gateway/**").permitAll()
                .pathMatchers("/favicon.ico").permitAll()
                .pathMatchers("/error").permitAll()
                // 其他所有路径都需要认证
                .anyExchange().authenticated()
            )
            // 禁用CSRF保护（网关服务不需要）
            .csrf(csrf -> csrf.disable())
            // 禁用HTTP Basic认证
            .httpBasic(httpBasic -> httpBasic.disable())
            // 禁用表单登录
            .formLogin(formLogin -> formLogin.disable())
            // 禁用登出
            .logout(logout -> logout.disable())
            .build();
    }
}