package com.ppcex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAssetMapper extends BaseMapper<UserAsset> {

    @Select("SELECT * FROM user_asset WHERE user_id = #{userId} AND deleted = 0")
    UserAsset selectByUserId(@Param("userId") Long userId);
}