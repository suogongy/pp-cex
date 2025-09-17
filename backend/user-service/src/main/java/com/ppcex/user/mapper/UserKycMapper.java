package com.ppcex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserKyc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserKycMapper extends BaseMapper<UserKyc> {

    @Select("SELECT * FROM user_kyc WHERE user_id = #{userId} AND deleted = 0")
    UserKyc selectByUserId(@Param("userId") Long userId);

    @Update("UPDATE user_info SET kyc_status = #{kycStatus} WHERE id = #{userId}")
    void updateUserKycStatus(@Param("userId") Long userId, @Param("kycStatus") Integer kycStatus);
}