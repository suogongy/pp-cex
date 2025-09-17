package com.ppcex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ppcex.user.dto.UserInfoResponse;
import com.ppcex.user.dto.UserLoginRequest;
import com.ppcex.user.dto.UserLoginResponse;
import com.ppcex.user.dto.UserRegisterRequest;
import com.ppcex.user.entity.UserInfo;
import com.ppcex.user.entity.UserLoginLog;
import com.ppcex.user.mapper.UserInfoMapper;
import com.ppcex.user.mapper.UserLoginLogMapper;
import com.ppcex.common.util.JwtUtil;
import com.ppcex.user.security.UserDetailsServiceImpl;
import com.ppcex.user.service.UserService;
import com.ppcex.user.util.PasswordUtil;
import com.ppcex.user.util.UserNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;
    private final UserLoginLogMapper userLoginLogMapper;
        private final UserDetailsServiceImpl userDetailsService;
    private final PasswordUtil passwordUtil;
    private final UserNoGenerator userNoGenerator;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(UserRegisterRequest registerRequest) {
        log.info("用户注册开始: {}", registerRequest.getUsername());

        // 检查用户名是否已存在
        if (checkUsernameExists(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (checkEmailExists(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 检查手机号是否已存在
        if (checkPhoneExists(registerRequest.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 验证密码强度
        if (!passwordUtil.isStrongPassword(registerRequest.getPassword())) {
            throw new RuntimeException("密码强度不足");
        }

        // 验证确认密码
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // TODO: 验证验证码
        // verifyCode(registerRequest.getEmail(), registerRequest.getVerificationCode());

        // 创建用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserNo(userNoGenerator.generateUserNo());
        userInfo.setUsername(registerRequest.getUsername());
        userInfo.setEmail(registerRequest.getEmail());
        userInfo.setPhone(registerRequest.getPhone());

        // 生成密码盐值并加密密码
        String salt = passwordUtil.generateSalt();
        String passwordHash = passwordUtil.encodePassword(registerRequest.getPassword(), salt);
        userInfo.setPasswordHash(passwordHash);
        userInfo.setSalt(salt);

        userInfo.setStatus(1); // 正常状态
        userInfo.setKycStatus(0); // 未认证
        userInfo.setGoogleAuthEnabled(0); // 未启用Google 2FA
        userInfo.setLoginFailedCount(0);
        userInfo.setLanguage("zh-CN");
        userInfo.setTimezone("Asia/Shanghai");
        userInfo.setRegisterTime(LocalDateTime.now());

        // 处理邀请码
        if (registerRequest.getInviteCode() != null && !registerRequest.getInviteCode().trim().isEmpty()) {
            UserInfo inviter = userInfoMapper.selectOne(
                new QueryWrapper<UserInfo>()
                    .eq("invite_code", registerRequest.getInviteCode())
                    .eq("status", 1)
            );
            if (inviter != null) {
                userInfo.setInviterId(inviter.getId());
            }
        }

        // 生成邀请码
        userInfo.setInviteCode(userNoGenerator.generateInviteCode());

        // 保存用户信息
        userInfoMapper.insert(userInfo);

        log.info("用户注册成功: {}, 用户编号: {}", registerRequest.getUsername(), userInfo.getUserNo());
    }

    @Override
    public UserLoginResponse login(UserLoginRequest loginRequest) {
        log.info("用户登录开始: {}", loginRequest.getUsername());

        try {
            // 验证用户凭证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserInfo userInfo = userInfoMapper.selectByUsername(userDetails.getUsername());

            // 检查是否需要Google 2FA
            if (userInfo.getGoogleAuthEnabled() == 1) {
                if (loginRequest.getGoogleCode() == null || loginRequest.getGoogleCode().trim().isEmpty()) {
                    // 返回需要Google 2FA的状态
                    UserLoginResponse response = new UserLoginResponse();
                    response.setRequireGoogle2FA(true);
                    response.setSessionId(userNoGenerator.generateSessionId());
                    return response;
                }

                // TODO: 验证Google 2FA
                // verifyGoogle2FA(userInfo, loginRequest.getGoogleCode());
            }

            // 生成JWT token
            String accessToken = JwtUtil.createUserToken(
                userInfo.getId(),
                userInfo.getUsername(),
                "USER"
            );

            String refreshToken = JwtUtil.createRefreshToken(
                userInfo.getId()
            );

            // 记录登录日志
            recordLoginLog(userInfo.getId(), 1, null, true);

            // 更新用户登录信息
            updateLoginInfo(userInfo.getId());

            // 重置登录失败次数
            userInfoMapper.resetLoginFailedCount(userInfo.getId());

            // 构建响应
            UserInfoResponse userInfoResponse = new UserInfoResponse();
            BeanUtils.copyProperties(userInfo, userInfoResponse);

            UserLoginResponse response = new UserLoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                JwtUtil.getTokenRemainingTime(accessToken),
                userInfoResponse,
                false,
                null
            );

            log.info("用户登录成功: {}", loginRequest.getUsername());
            return response;

        } catch (BadCredentialsException e) {
            // 处理登录失败
            handleLoginFailure(loginRequest.getUsername());
            throw new RuntimeException("用户名或密码错误");
        } catch (Exception e) {
            log.error("用户登录失败: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        try {
            String username = JwtUtil.getUsernameFromToken(token);
            UserInfo userInfo = userInfoMapper.selectByUsername(username);

            if (userInfo != null) {
                // 记录登出日志
                recordLoginLog(userInfo.getId(), 1, "用户主动登出", true);
                log.info("用户登出成功: {}", username);
            }
        } catch (Exception e) {
            log.error("用户登出失败", e);
        }
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!JwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        String tokenType = JwtUtil.getTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("不是刷新令牌");
        }

        String username = JwtUtil.getUsernameFromToken(refreshToken);
        Long userId = JwtUtil.getClaimFromToken(refreshToken, "userId");

        // 检查用户状态
        checkUserStatus(userId);

        // 生成新的访问令牌
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return JwtUtil.createUserToken(
            userId,
            username,
            "USER"
        );
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(userInfo, response);
        return response;
    }

    @Override
    public UserInfoResponse updateUserInfo(Long userId, UserInfoResponse userInfoRequest) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户信息
        if (userInfoRequest.getNickname() != null) {
            userInfo.setNickname(userInfoRequest.getNickname());
        }
        if (userInfoRequest.getAvatar() != null) {
            userInfo.setAvatar(userInfoRequest.getAvatar());
        }
        if (userInfoRequest.getCountry() != null) {
            userInfo.setCountry(userInfoRequest.getCountry());
        }
        if (userInfoRequest.getLanguage() != null) {
            userInfo.setLanguage(userInfoRequest.getLanguage());
        }
        if (userInfoRequest.getTimezone() != null) {
            userInfo.setTimezone(userInfoRequest.getTimezone());
        }

        userInfoMapper.updateById(userInfo);

        UserInfoResponse response = new UserInfoResponse();
        BeanUtils.copyProperties(userInfo, response);
        return response;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordUtil.matches(oldPassword, userInfo.getPasswordHash(), userInfo.getSalt())) {
            throw new RuntimeException("原密码错误");
        }

        // 验证新密码强度
        if (!passwordUtil.isStrongPassword(newPassword)) {
            throw new RuntimeException("新密码强度不足");
        }

        // 生成新的密码盐值并加密密码
        String newSalt = passwordUtil.generateSalt();
        String newPasswordHash = passwordUtil.encodePassword(newPassword, newSalt);

        userInfo.setPasswordHash(newPasswordHash);
        userInfo.setSalt(newSalt);
        userInfoMapper.updateById(userInfo);

        log.info("用户修改密码成功: {}", userInfo.getUsername());
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userInfoMapper.selectByUsername(username) != null;
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userInfoMapper.selectByEmail(email) != null;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return userInfoMapper.selectByPhone(phone) != null;
    }

    @Override
    public Long getUserIdByUsername(String username) {
        UserInfo userInfo = userInfoMapper.selectByUsername(username);
        if (userInfo == null) {
            userInfo = userInfoMapper.selectByEmail(username);
        }
        if (userInfo == null) {
            userInfo = userInfoMapper.selectByPhone(username);
        }
        return userInfo != null ? userInfo.getId() : null;
    }

    @Override
    public void checkUserStatus(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo == null) {
            throw new RuntimeException("用户不存在");
        }

        if (userInfo.getStatus() != 1) {
            throw new RuntimeException("用户已被冻结或注销");
        }

        if (userInfo.getAccountLockedUntil() != null &&
            userInfo.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("用户账户已被锁定");
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(String username) {
        UserInfo userInfo = userInfoMapper.selectByUsername(username);
        if (userInfo == null) {
            return;
        }

        int failedCount = userInfo.getLoginFailedCount() + 1;
        userInfoMapper.updateLoginFailedCount(userInfo.getId(), failedCount);

        // 记录登录失败日志
        recordLoginLog(userInfo.getId(), 1, "用户名或密码错误", false);

        // 检查是否需要锁定账户
        int maxFailedAttempts = 5; // 最大失败次数
        if (failedCount >= maxFailedAttempts) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30); // 锁定30分钟
            userInfoMapper.lockAccount(userInfo.getId(), lockUntil);
            log.warn("用户账户已被锁定: {}, 锁定至: {}", username, lockUntil);
        }
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, Integer loginType, String failReason, Boolean success) {
        try {
            UserLoginLog loginLog = new UserLoginLog();
            loginLog.setUserId(userId);
            loginLog.setLoginType(loginType);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setLoginResult(success ? 1 : 2);
            loginLog.setFailReason(failReason);

            // TODO: 获取客户端IP和设备信息
            // loginLog.setLoginIp(getClientIp());
            // loginLog.setDeviceInfo(getDeviceInfo());

            userLoginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /**
     * 更新用户登录信息
     */
    private void updateLoginInfo(Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            // TODO: 获取客户端IP
            String clientIp = "127.0.0.1";

            userInfoMapper.updateLoginInfo(userId, now, clientIp);
        } catch (Exception e) {
            log.error("更新用户登录信息失败", e);
        }
    }
}