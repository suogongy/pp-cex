package com.ppcex.user.security;

import com.ppcex.user.entity.UserInfo;
import com.ppcex.user.mapper.UserInfoMapper;
import com.ppcex.user.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserInfoMapper userInfoMapper;
    private final PasswordUtil passwordUtil;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 尝试用用户名查询
        UserInfo userInfo = userInfoMapper.selectByUsername(username);

        // 如果用户名查询失败，尝试用邮箱查询
        if (userInfo == null) {
            userInfo = userInfoMapper.selectByEmail(username);
        }

        // 如果邮箱查询失败，尝试用手机号查询
        if (userInfo == null) {
            userInfo = userInfoMapper.selectByPhone(username);
        }

        if (userInfo == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态
        if (userInfo.getStatus() != 1) {
            log.warn("用户已被冻结或注销: {}", username);
            throw new BadCredentialsException("用户已被冻结或注销");
        }

        // 检查账户是否被锁定
        if (userInfo.getAccountLockedUntil() != null &&
            userInfo.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            log.warn("用户账户已被锁定: {}", username);
            throw new BadCredentialsException("用户账户已被锁定");
        }

        // 验证密码
        if (!passwordUtil.matches(password, userInfo.getPasswordHash(), userInfo.getSalt())) {
            log.warn("密码验证失败: {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 构建UserDetails
        UserDetails userDetails = User.builder()
                .username(userInfo.getUsername())
                .password(userInfo.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(userInfo.getAccountLockedUntil() != null &&
                               userInfo.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now()))
                .credentialsExpired(false)
                .disabled(userInfo.getStatus() != 1)
                .build();

        // 创建认证令牌
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                password,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}