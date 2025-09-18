package com.ppcex.match.security;

import com.ppcex.common.util.UserContext;
import com.ppcex.common.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @PostConstruct
    public void init() {
        log.info("MatchService JwtAuthenticationFilter 被创建并注册到Spring容器");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader("Authorization");
            String path = request.getServletPath();

            log.debug("JWT过滤器处理请求路径: {}", path);

            // 如果有Bearer token，尝试解析
            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (jwtService.validateToken(token)) {
                    // 解析userId并设置到UserContext
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

                    UserContext.setCurrentUserId(userId);
                    log.debug("用户ID {} 已设置到UserContext", userId);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            // JWT无效，返回401让前端跳转登录
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token无效或已过期");
        } finally {
            // 请求完成后清除用户上下文
            UserContext.clear();
        }
    }

    /**
     * 判断是否需要跳过JWT验证的路径
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/match/api/v1/orderbook/") ||
               path.startsWith("/match/api/v1/trades/") ||
               path.startsWith("/match/api/v1/ticker/") ||
               path.startsWith("/doc.html") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.equals("/actuator/health") ||
               path.equals("/actuator/info");
    }
}