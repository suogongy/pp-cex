package com.ppcex.user.mapper;

import com.ppcex.user.entity.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserInfoMapperTest {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    void insert_ShouldInsertUserSuccessfully() {
        UserInfo user = createTestUser();

        int result = userInfoMapper.insert(user);

        assertEquals(1, result);
        assertNotNull(user.getId());
    }

    @Test
    void selectById_ShouldReturnUser() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        UserInfo foundUser = userInfoMapper.selectById(user.getId());

        assertNotNull(foundUser);
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void selectByUsername_ShouldReturnUser() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        UserInfo foundUser = userInfoMapper.selectByUsername(user.getUsername());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void selectByEmail_ShouldReturnUser() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        UserInfo foundUser = userInfoMapper.selectByEmail(user.getEmail());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void selectByPhone_ShouldReturnUser() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        UserInfo foundUser = userInfoMapper.selectByPhone(user.getPhone());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void updateById_ShouldUpdateUser() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        user.setNickname("Updated Nickname");
        user.setAvatar("https://example.com/avatar.jpg");

        int result = userInfoMapper.updateById(user);

        assertEquals(1, result);

        UserInfo updatedUser = userInfoMapper.selectById(user.getId());
        assertEquals("Updated Nickname", updatedUser.getNickname());
        assertEquals("https://example.com/avatar.jpg", updatedUser.getAvatar());
    }

    @Test
    void updateLoginFailedCount_ShouldUpdateCount() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        int failedCount = 3;
        userInfoMapper.updateLoginFailedCount(user.getId(), failedCount);

        UserInfo updatedUser = userInfoMapper.selectById(user.getId());
        assertEquals(failedCount, updatedUser.getLoginFailedCount());
    }

    @Test
    void resetLoginFailedCount_ShouldResetToZero() {
        UserInfo user = createTestUser();
        user.setLoginFailedCount(5);
        userInfoMapper.insert(user);

        userInfoMapper.resetLoginFailedCount(user.getId());

        UserInfo updatedUser = userInfoMapper.selectById(user.getId());
        assertEquals(0, updatedUser.getLoginFailedCount());
    }

    @Test
    void lockAccount_ShouldSetLockTime() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        userInfoMapper.lockAccount(user.getId(), lockUntil);

        UserInfo updatedUser = userInfoMapper.selectById(user.getId());
        assertNotNull(updatedUser.getAccountLockedUntil());
        assertTrue(updatedUser.getAccountLockedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void updateLoginInfo_ShouldUpdateLoginDetails() {
        UserInfo user = createTestUser();
        userInfoMapper.insert(user);

        LocalDateTime loginTime = LocalDateTime.now();
        String loginIp = "192.168.1.100";

        userInfoMapper.updateLoginInfo(user.getId(), loginTime, loginIp);

        UserInfo updatedUser = userInfoMapper.selectById(user.getId());
        assertEquals(loginTime, updatedUser.getLastLoginTime());
        assertEquals(loginIp, updatedUser.getLastLoginIp());
    }

    @Test
    void selectList_ShouldReturnAllUsers() {
        UserInfo user1 = createTestUser();
        UserInfo user2 = createTestUser();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPhone("13800138001");

        userInfoMapper.insert(user1);
        userInfoMapper.insert(user2);

        List<UserInfo> users = userInfoMapper.selectList(null);

        assertTrue(users.size() >= 2);
    }

    private UserInfo createTestUser() {
        UserInfo user = new UserInfo();
        user.setUserNo("U202409171200000001");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setPasswordHash("hashedPassword");
        user.setSalt("salt");
        user.setStatus(1);
        user.setKycStatus(0);
        user.setGoogleAuthEnabled(0);
        user.setLoginFailedCount(0);
        user.setLanguage("zh-CN");
        user.setTimezone("Asia/Shanghai");
        user.setRegisterTime(LocalDateTime.now());
        user.setDeleted(0);
        return user;
    }
}