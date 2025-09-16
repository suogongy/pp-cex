package com.ppcex.user.service;

import com.ppcex.user.dto.UserLoginRequest;
import com.ppcex.user.dto.UserLoginResponse;
import com.ppcex.user.dto.UserRegisterRequest;
import com.ppcex.user.dto.UserInfoResponse;

public interface UserService {

    /**
     * 用户注册
     */
    void register(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     */
    UserLoginResponse login(UserLoginRequest loginRequest);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新token
     */
    String refreshToken(String refreshToken);

    /**
     * 获取用户信息
     */
    UserInfoResponse getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UserInfoResponse userInfoRequest);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查手机号是否已存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 根据用户名获取用户ID
     */
    Long getUserIdByUsername(String username);

    /**
     * 检查用户状态
     */
    void checkUserStatus(Long userId);
}