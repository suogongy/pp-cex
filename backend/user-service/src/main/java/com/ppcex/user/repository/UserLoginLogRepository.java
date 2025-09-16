package com.ppcex.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLoginLogRepository extends BaseMapper<UserLoginLog> {
}