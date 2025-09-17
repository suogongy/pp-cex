package com.ppcex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Select("SELECT * FROM user_info WHERE username = #{username} AND deleted = 0")
    UserInfo selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user_info WHERE email = #{email} AND deleted = 0")
    UserInfo selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user_info WHERE phone = #{phone} AND deleted = 0")
    UserInfo selectByPhone(@Param("phone") String phone);

    @Update("UPDATE user_info SET login_failed_count = #{failedCount} WHERE id = #{userId}")
    void updateLoginFailedCount(@Param("userId") Long userId, @Param("failedCount") int failedCount);

    @Update("UPDATE user_info SET login_failed_count = 0 WHERE id = #{userId}")
    void resetLoginFailedCount(@Param("userId") Long userId);

    @Update("UPDATE user_info SET account_locked_until = #{lockUntil} WHERE id = #{userId}")
    void lockAccount(@Param("userId") Long userId, @Param("lockUntil") java.time.LocalDateTime lockUntil);

    @Update("UPDATE user_info SET last_login_time = #{loginTime}, last_login_ip = #{loginIp} WHERE id = #{userId}")
    void updateLoginInfo(@Param("userId") Long userId, @Param("loginTime") java.time.LocalDateTime loginTime, @Param("loginIp") String loginIp);
}