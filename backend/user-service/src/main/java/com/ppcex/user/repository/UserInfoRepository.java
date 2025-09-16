package com.ppcex.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserInfoRepository extends BaseMapper<UserInfo> {

    /**
     * 根据用户名查询用户信息
     */
    UserInfo selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户信息
     */
    UserInfo selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户信息
     */
    UserInfo selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户编号查询用户信息
     */
    UserInfo selectByUserNo(@Param("userNo") String userNo);

    /**
     * 更新登录信息
     */
    int updateLoginInfo(@Param("userId") Long userId,
                        @Param("loginTime") java.time.LocalDateTime loginTime,
                        @Param("loginIp") String loginIp);

    /**
     * 更新登录失败次数
     */
    int updateLoginFailedCount(@Param("userId") Long userId, @Param("count") Integer count);

    /**
     * 重置登录失败次数
     */
    int resetLoginFailedCount(@Param("userId") Long userId);

    /**
     * 锁定账户
     */
    int lockAccount(@Param("userId") Long userId, @Param("lockUntil") java.time.LocalDateTime lockUntil);

    /**
     * 解锁账户
     */
    int unlockAccount(@Param("userId") Long userId);
}