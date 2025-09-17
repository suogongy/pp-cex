package com.ppcex.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppcex.user.dto.ApiResponse;
import com.ppcex.common.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @PostConstruct
    public void init() {
        log.info("JwtAuthenticationFilter 被创建并注册到Spring容器");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 获取Authorization header
            String authorizationHeader = request.getHeader("Authorization");
            String path = request.getServletPath();

            log.info("JWT过滤器处理请求路径: {}, Authorization: {}", path, authorizationHeader);
            log.info("请求方法: {}, RemoteAddr: {}", request.getMethod(), request.getRemoteAddr());

            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                log.debug("提取的JWT token: {}", token.substring(0, Math.min(20, token.length())) + "...");

                // 验证token
                if (jwtService.validateToken(token)) {
                    // 处理userId可能是Integer或Long的情况
                    Object userIdObj = jwtService.getClaimFromToken(token, "userId");
                    Long userId;
                    if (userIdObj instanceof Integer) {
                        userId = ((Integer) userIdObj).longValue();
                    } else if (userIdObj instanceof Long) {
                        userId = (Long) userIdObj;
                    } else {
                        log.error("JWT token中的userId类型不支持: {}", userIdObj.getClass().getName());
                        throw new RuntimeException("JWT token中的userId类型不支持");
                    }

                    String username = jwtService.getUsernameFromToken(token);
                    log.debug("JWT token验证成功，用户ID: {}, 用户名: {}", userId, username);

                    // 如果SecurityContext中没有认证信息，则进行认证
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        try {
                            // 使用用户ID加载用户详情
                            UserDetails userDetails = ((UserDetailsServiceImpl) userDetailsService).loadUserByUserId(userId);

                            log.debug("用户详情加载成功: {}, 权限: {}", userDetails.getUsername(), userDetails.getAuthorities());

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.info("用户 {} (ID: {}) 已通过JWT认证", username, userId);
                        } catch (UsernameNotFoundException e) {
                            log.error("用户详情加载失败: {}", e.getMessage());
                            // 用户不存在或被禁用，继续过滤器链但不要设置认证
                            filterChain.doFilter(request, response);
                            return;
                        }
                    } else {
                        log.debug("SecurityContext中已存在认证信息: {}", SecurityContextHolder.getContext().getAuthentication());
                    }
                } else {
                    log.warn("JWT token验证失败");
                }
            } else {
                log.debug("没有找到Authorization header或格式不正确");
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage(), e);
            handleAuthenticationException(response, e);
        }
    }

    /**
     * 处理认证异常
     */
    private void handleAuthenticationException(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<String> apiResponse;
        if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
            apiResponse = ApiResponse.error(401, "Token已过期");
        } else if (e instanceof io.jsonwebtoken.UnsupportedJwtException) {
            apiResponse = ApiResponse.error(401, "不支持的Token");
        } else if (e instanceof io.jsonwebtoken.MalformedJwtException) {
            apiResponse = ApiResponse.error(401, "Token格式错误");
        } else if (e instanceof java.lang.SecurityException) {
            apiResponse = ApiResponse.error(401, "Token签名验证失败");
        } else {
            apiResponse = ApiResponse.error(401, "认证失败: " + e.getMessage());
        }

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * 判断是否需要跳过JWT验证的路径
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        log.info("shouldNotFilter检查路径: {}", path);
        boolean shouldSkip = (path.startsWith("/api/v1/auth/") && !path.equals("/api/v1/auth/logout")) ||
               path.startsWith("/doc.html") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info");
        log.info("路径 {} 是否跳过JWT验证: {}", path, shouldSkip);
        return shouldSkip;
    }
}