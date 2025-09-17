package com.ppcex.user.service;

import com.ppcex.user.dto.UserLoginRequest;
import com.ppcex.user.dto.UserRegisterRequest;
import com.ppcex.user.entity.UserInfo;
import com.ppcex.user.mapper.UserInfoMapper;
import com.ppcex.user.service.impl.UserServiceImpl;
import com.ppcex.user.util.PasswordUtil;
import com.ppcex.user.util.UserNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserInfoMapper userInfoMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private UserNoGenerator userNoGenerator;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("13800138000");
        registerRequest.setPassword("Password123!");
        registerRequest.setConfirmPassword("Password123!");

        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

        userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setPhone("13800138000");
        userInfo.setPasswordHash("hashedPassword");
        userInfo.setSalt("salt");
        userInfo.setStatus(1);
        userInfo.setRegisterTime(LocalDateTime.now());
    }

    @Test
    void register_Success() {
        when(passwordUtil.isStrongPassword(anyString())).thenReturn(true);
        when(userNoGenerator.generateUserNo()).thenReturn("U202409171200000001");
        when(userNoGenerator.generateInviteCode()).thenReturn("ABC12345");
        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail(anyString())).thenReturn(null);
        when(userInfoMapper.selectByPhone(anyString())).thenReturn(null);
        when(userInfoMapper.insert(any(UserInfo.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.register(registerRequest));

        verify(userInfoMapper, times(1)).insert(any(UserInfo.class));
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        when(userInfoMapper.selectByUsername("testuser")).thenReturn(userInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userInfoMapper, never()).insert(any(UserInfo.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail("test@example.com")).thenReturn(userInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("邮箱已存在", exception.getMessage());
        verify(userInfoMapper, never()).insert(any(UserInfo.class));
    }

    @Test
    void register_PhoneExists_ThrowsException() {
        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail(anyString())).thenReturn(null);
        when(userInfoMapper.selectByPhone("13800138000")).thenReturn(userInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("手机号已存在", exception.getMessage());
        verify(userInfoMapper, never()).insert(any(UserInfo.class));
    }

    @Test
    void register_WeakPassword_ThrowsException() {
        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail(anyString())).thenReturn(null);
        when(userInfoMapper.selectByPhone(anyString())).thenReturn(null);
        when(passwordUtil.isStrongPassword(anyString())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("密码强度不足", exception.getMessage());
        verify(userInfoMapper, never()).insert(any(UserInfo.class));
    }

    @Test
    void register_PasswordMismatch_ThrowsException() {
        registerRequest.setConfirmPassword("DifferentPassword123!");

        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail(anyString())).thenReturn(null);
        when(userInfoMapper.selectByPhone(anyString())).thenReturn(null);
        when(passwordUtil.isStrongPassword(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("两次输入的密码不一致", exception.getMessage());
        verify(userInfoMapper, never()).insert(any(UserInfo.class));
    }

    @Test
    void checkUsernameExists_True() {
        when(userInfoMapper.selectByUsername("testuser")).thenReturn(userInfo);

        boolean result = userService.checkUsernameExists("testuser");

        assertTrue(result);
    }

    @Test
    void checkUsernameExists_False() {
        when(userInfoMapper.selectByUsername("testuser")).thenReturn(null);

        boolean result = userService.checkUsernameExists("testuser");

        assertFalse(result);
    }

    @Test
    void checkEmailExists_True() {
        when(userInfoMapper.selectByEmail("test@example.com")).thenReturn(userInfo);

        boolean result = userService.checkEmailExists("test@example.com");

        assertTrue(result);
    }

    @Test
    void checkEmailExists_False() {
        when(userInfoMapper.selectByEmail("test@example.com")).thenReturn(null);

        boolean result = userService.checkEmailExists("test@example.com");

        assertFalse(result);
    }

    @Test
    void checkPhoneExists_True() {
        when(userInfoMapper.selectByPhone("13800138000")).thenReturn(userInfo);

        boolean result = userService.checkPhoneExists("13800138000");

        assertTrue(result);
    }

    @Test
    void checkPhoneExists_False() {
        when(userInfoMapper.selectByPhone("13800138000")).thenReturn(null);

        boolean result = userService.checkPhoneExists("13800138000");

        assertFalse(result);
    }

    @Test
    void getUserIdByUsername_Found() {
        when(userInfoMapper.selectByUsername("testuser")).thenReturn(userInfo);

        Long result = userService.getUserIdByUsername("testuser");

        assertEquals(1L, result);
    }

    @Test
    void getUserIdByUsername_NotFound() {
        when(userInfoMapper.selectByUsername(anyString())).thenReturn(null);
        when(userInfoMapper.selectByEmail(anyString())).thenReturn(null);
        when(userInfoMapper.selectByPhone(anyString())).thenReturn(null);

        Long result = userService.getUserIdByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    void checkUserStatus_UserNotFound_ThrowsException() {
        when(userInfoMapper.selectById(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.checkUserStatus(1L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void checkUserStatus_UserFrozen_ThrowsException() {
        userInfo.setStatus(2);
        when(userInfoMapper.selectById(1L)).thenReturn(userInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.checkUserStatus(1L);
        });

        assertEquals("用户已被冻结或注销", exception.getMessage());
    }

    @Test
    void checkUserStatus_UserLocked_ThrowsException() {
        userInfo.setStatus(1);
        userInfo.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
        when(userInfoMapper.selectById(1L)).thenReturn(userInfo);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.checkUserStatus(1L);
        });

        assertEquals("用户账户已被锁定", exception.getMessage());
    }

    @Test
    void checkUserStatus_ValidUser_NoException() {
        userInfo.setStatus(1);
        userInfo.setAccountLockedUntil(null);
        when(userInfoMapper.selectById(1L)).thenReturn(userInfo);

        assertDoesNotThrow(() -> userService.checkUserStatus(1L));
    }
}