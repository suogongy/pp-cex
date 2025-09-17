package com.ppcex.user.security;

import com.ppcex.user.entity.UserInfo;
import com.ppcex.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
            throw new UsernameNotFoundException("用户已被冻结或注销");
        }

        // 检查账户是否被锁定
        if (userInfo.getAccountLockedUntil() != null &&
            userInfo.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            log.warn("用户账户已被锁定: {}", username);
            throw new UsernameNotFoundException("用户账户已被锁定");
        }

        return User.builder()
                .username(userInfo.getUsername())
                .password(userInfo.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(userInfo.getAccountLockedUntil() != null &&
                               userInfo.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now()))
                .credentialsExpired(false)
                .disabled(userInfo.getStatus() != 1)
                .build();
    }

    /**
     * 根据用户ID加载用户详情
     */
    public UserDetails loadUserByUserId(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        // 检查用户状态
        if (userInfo.getStatus() != 1) {
            log.warn("用户已被冻结或注销: {}", userId);
            throw new UsernameNotFoundException("用户已被冻结或注销");
        }

        // 检查账户是否被锁定
        if (userInfo.getAccountLockedUntil() != null &&
            userInfo.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            log.warn("用户账户已被锁定: {}", userId);
            throw new UsernameNotFoundException("用户账户已被锁定");
        }

        return User.builder()
                .username(userInfo.getUsername())
                .password(userInfo.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}