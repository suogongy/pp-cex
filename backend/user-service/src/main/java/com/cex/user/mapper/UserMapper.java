package com.cex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cex.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE email = #{email} AND deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户编号查询用户
     *
     * @param userNo 用户编号
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE user_no = #{userNo} AND deleted = 0")
    User selectByUserNo(@Param("userNo") String userNo);

    /**
     * 根据API密钥查询用户
     *
     * @param apiKey API密钥
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE api_key = #{apiKey} AND deleted = 0")
    User selectByApiKey(@Param("apiKey") String apiKey);

    /**
     * 更新登录信息
     *
     * @param userId      用户ID
     * @param loginTime   登录时间
     * @param loginIp     登录IP
     * @param failCount   失败次数
     * @param lockTime    锁定时间
     * @return 影响行数
     */
    @Update("UPDATE user_info SET last_login_time = #{loginTime}, last_login_ip = #{loginIp}, " +
            "login_fail_count = #{failCount}, lock_time = #{lockTime}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateLoginInfo(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime,
                       @Param("loginIp") String loginIp, @Param("failCount") Integer failCount,
                       @Param("lockTime") LocalDateTime lockTime);

    /**
     * 重置登录失败次数
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_info SET login_fail_count = 0, lock_time = NULL, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int resetLoginFailCount(@Param("userId") Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE user_info SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 更新KYC状态
     *
     * @param userId     用户ID
     * @param kycStatus  KYC状态
     * @return 影响行数
     */
    @Update("UPDATE user_info SET kyc_status = #{kycStatus}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateKycStatus(@Param("userId") Long userId, @Param("kycStatus") Integer kycStatus);

    /**
     * 启用Google认证
     *
     * @param userId              用户ID
     * @param googleAuthSecret    Google认证密钥
     * @param googleAuthEnabled   是否启用
     * @return 影响行数
     */
    @Update("UPDATE user_info SET google_auth_secret = #{googleAuthSecret}, " +
            "google_auth_enabled = #{googleAuthEnabled}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int enableGoogleAuth(@Param("userId") Long userId, @Param("googleAuthSecret") String googleAuthSecret,
                         @Param("googleAuthEnabled") Boolean googleAuthEnabled);

    /**
     * 更新邮箱验证状态
     *
     * @param userId         用户ID
     * @param emailVerified  验证状态
     * @return 影响行数
     */
    @Update("UPDATE user_info SET email_verified = #{emailVerified}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateEmailVerified(@Param("userId") Long userId, @Param("emailVerified") Boolean emailVerified);

    /**
     * 更新手机验证状态
     *
     * @param userId         用户ID
     * @param phoneVerified  验证状态
     * @return 影响行数
     */
    @Update("UPDATE user_info SET phone_verified = #{phoneVerified}, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updatePhoneVerified(@Param("userId") Long userId, @Param("phoneVerified") Boolean phoneVerified);

    /**
     * 更新API密钥
     *
     * @param userId     用户ID
     * @param apiKey     API密钥
     * @param apiSecret  API密钥密钥
     * @return 影响行数
     */
    @Update("UPDATE user_info SET api_key = #{apiKey}, api_secret = #{apiSecret}, " +
            "api_enabled = TRUE, update_time = NOW() " +
            "WHERE id = #{userId} AND deleted = 0")
    int updateApiKeys(@Param("userId") Long userId, @Param("apiKey") String apiKey,
                     @Param("apiSecret") String apiSecret);

    /**
     * 禁用API
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_info SET api_enabled = FALSE, api_key = NULL, api_secret = NULL, " +
            "update_time = NOW() WHERE id = #{userId} AND deleted = 0")
    int disableApi(@Param("userId") Long userId);

    /**
     * 根据邀请码查询邀请人
     *
     * @param inviteCode 邀请码
     * @return 用户信息
     */
    @Select("SELECT * FROM user_info WHERE invite_code = #{inviteCode} AND deleted = 0")
    User selectByInviteCode(@Param("inviteCode") String inviteCode);

    /**
     * 查询需要解锁的用户
     *
     * @param unlockTime 解锁时间
     * @return 用户列表
     */
    @Select("SELECT * FROM user_info WHERE lock_time <= #{unlockTime} AND status = 2 AND deleted = 0")
    List<User> selectLockedUsers(@Param("unlockTime") LocalDateTime unlockTime);

    /**
     * 分页查询用户列表
     *
     * @param page     分页参数
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @param status   状态
     * @param kycStatus KYC状态
     * @return 用户分页列表
     */
    @Select("<script>" +
            "SELECT * FROM user_info WHERE deleted = 0" +
            "<if test='username != null and username != \"\"'>" +
            " AND username LIKE CONCAT('%', #{username}, '%')" +
            "</if>" +
            "<if test='email != null and email != \"\"'>" +
            " AND email LIKE CONCAT('%', #{email}, '%')" +
            "</if>" +
            "<if test='phone != null and phone != \"\"'>" +
            " AND phone LIKE CONCAT('%', #{phone}, '%')" +
            "</if>" +
            "<if test='status != null'>" +
            " AND status = #{status}" +
            "</if>" +
            "<if test='kycStatus != null'>" +
            " AND kyc_status = #{kycStatus}" +
            "</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<User> selectUserPage(Page<User> page, @Param("username") String username,
                             @Param("email") String email, @Param("phone") String phone,
                             @Param("status") Integer status, @Param("kycStatus") Integer kycStatus);

    /**
     * 统计用户数量
     *
     * @param status   状态
     * @param kycStatus KYC状态
     * @return 用户数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_info WHERE deleted = 0" +
            "<if test='status != null'>" +
            " AND status = #{status}" +
            "</if>" +
            "<if test='kycStatus != null'>" +
            " AND kyc_status = #{kycStatus}" +
            "</if>" +
            "</script>")
    Long countUsers(@Param("status") Integer status, @Param("kycStatus") Integer kycStatus);

    /**
     * 查询活跃用户
     *
     * @param days 天数
     * @return 用户列表
     */
    @Select("SELECT * FROM user_info WHERE last_login_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted = 0 AND status = 1 ORDER BY last_login_time DESC")
    List<User> selectActiveUsers(@Param("days") Integer days);

    /**
     * 查询待KYC审核用户
     *
     * @return 用户列表
     */
    @Select("SELECT * FROM user_info WHERE kyc_status = 2 AND deleted = 0 AND status = 1 " +
            "ORDER BY register_time ASC")
    List<User> selectPendingKycUsers();

    /**
     * 批量更新用户状态
     *
     * @param userIds 用户ID列表
     * @param status  状态
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE user_info SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            " AND deleted = 0" +
            "</script>")
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);
}