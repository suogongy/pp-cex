package com.ppcex.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppcex.user.dto.ApiResponse;
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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 获取Authorization header
            String authorizationHeader = request.getHeader("Authorization");

            if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                // 验证token
                if (jwtTokenUtil.validateToken(token)) {
                    String username = jwtTokenUtil.getUsernameFromToken(token);

                    // 如果SecurityContext中没有认证信息，则进行认证
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户 {} 已通过JWT认证", username);
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT认证失败", e);
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
        } else if (e instanceof io.jsonwebtoken.SecurityException) {
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
        return path.startsWith("/user/api/v1/auth/") ||
               path.startsWith("/user/doc.html") ||
               path.startsWith("/user/swagger-ui") ||
               path.startsWith("/user/v3/api-docs") ||
               path.startsWith("/user/swagger-resources") ||
               path.equals("/user/actuator/health") ||
               path.equals("/user/actuator/info");
    }
}