package com.ppcex.notify.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.notify.entity.UserNotifyPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知偏好Mapper
 */
@Mapper
public interface UserNotifyPreferenceMapper extends BaseMapper<UserNotifyPreference> {
}